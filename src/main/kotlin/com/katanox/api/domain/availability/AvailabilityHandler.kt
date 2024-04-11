package com.katanox.api.domain.availability

import com.katanox.api.domain.problem.ProblemHandlerFunction
import com.katanox.api.ext.logOnNext
import org.slf4j.event.Level
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

@Component
class AvailabilityHandler(
    @Autowired private val availabilityService: AvailabilityService,
) : ProblemHandlerFunction() {
    companion object {
        const val PATH = "/availability"
    }

    override fun invoke(request: ServerRequest): Mono<ServerResponse> =
        availabilityService
            .search(request.bodyToMono(AvailabilityRequest::class.java))
            .logOnNext(Level.DEBUG)
            .collectList()
            .flatMap(ServerResponse.ok()::bodyValue)
}