package com.katanox.api.domain.booking

import com.katanox.api.domain.problem.ProblemHandlerFunction
import com.katanox.api.ext.logOnNext
import org.slf4j.event.Level
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

@Component
class BookingHandler(
    @Autowired private val bookingService: BookingService,
) : ProblemHandlerFunction() {
    companion object {
        const val PATH = "/booking"
    }

    override fun invoke(request: ServerRequest): Mono<ServerResponse> =
        bookingService
            .reserve(request.bodyToMono(BookingRequest::class.java))
            .logOnNext(Level.DEBUG)
            .flatMap(ServerResponse.accepted()::bodyValue)
}