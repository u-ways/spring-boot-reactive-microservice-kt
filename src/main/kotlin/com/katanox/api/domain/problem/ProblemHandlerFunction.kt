package com.katanox.api.domain.problem

import com.katanox.api.config.TraceabilityConfig.Companion.CORRELATION_HTTP_HEADER_KEY
import com.katanox.api.domain.problem.ProblemDetailBuilder.Companion.problemDetail
import com.katanox.api.ext.logOnNext
import org.slf4j.event.Level
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.ProblemDetail
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

/**
 * A handler function that handles [Problem] exceptions and returns a [ProblemResponse] accordingly.
 *
 * It implements the [ProblemDetail]s [RFC7807](https://tools.ietf.org/html/rfc7807) and tops it up with
 * the correlation ID and timestamp in the response body.
 *
 * This allows a standardized way to handle [Problem] exceptions that can be used across multiple handlers.
 * On top of that, it also sets the correlation ID in the response body to track a request across multiple services.
 */
abstract class ProblemHandlerFunction : HandlerFunction<ServerResponse> {
    abstract fun invoke(request: ServerRequest): Mono<ServerResponse>

    final override fun handle(request: ServerRequest): Mono<ServerResponse> =
        invoke(request).onErrorResume(Problem::class.java) { problem ->
            val error = problemDetail()
                .withProblem(problem)
                .withRequest(request)
                .build()

            ServerResponse
                .status(error.status)
                .contentType(APPLICATION_JSON)
                .header(CORRELATION_HTTP_HEADER_KEY, error.properties!![CORRELATION_HTTP_HEADER_KEY].toString())
                .bodyValue(error)
                .logOnNext(Level.ERROR) { error.toString() }
        }
}
