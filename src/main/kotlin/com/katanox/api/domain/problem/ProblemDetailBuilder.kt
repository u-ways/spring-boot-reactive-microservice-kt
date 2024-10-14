package com.katanox.api.domain.problem

import com.katanox.api.config.TraceabilityConfig
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.reactive.function.server.ServerRequest

/**
 * Builder for [ProblemDetail] instances.
 */
class ProblemDetailBuilder private constructor() {
    private lateinit var problem: Problem
    private lateinit var request: ServerRequest

    internal fun withProblem(problem: Problem): ProblemDetailBuilder =
        apply {
            this.problem = problem
        }

    internal fun withRequest(request: ServerRequest): ProblemDetailBuilder =
        apply {
            this.request = request
        }

    internal fun build(): ProblemDetail {
        val status =
            when (problem) {
                is Problem.NotFound -> HttpStatus.NOT_FOUND
                is Problem.NotAvailable -> HttpStatus.CONFLICT
                is Problem.InvalidInput -> HttpStatus.BAD_REQUEST
                is Problem.BadGateway -> HttpStatus.BAD_GATEWAY
            }

        val correlationId = (
            request.exchange()
                .getAttribute(TraceabilityConfig.CORRELATION_CONTEXT_KEY)
                ?: TraceabilityConfig.CORRELATION_ID_PROVIDER.invoke()
        )

        return ProblemDetail
            .forStatus(status)
            .apply {
                type = request.uri()
                title = status.reasonPhrase
                detail = problem.message
                properties =
                    mapOf(
                        TraceabilityConfig.CORRELATION_HTTP_HEADER_KEY to correlationId,
                        Problem::timestamp.name to problem.timestamp,
                    )
            }
    }

    companion object {
        @JvmStatic
        internal fun problemDetail() = ProblemDetailBuilder()
    }
}
