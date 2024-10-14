package com.katanox.api.domain.hotels

import com.katanox.api.domain.problem.Problem
import com.katanox.api.ext.logOnNext
import com.katanox.api.sql.tables.pojos.Hotels
import org.slf4j.event.Level
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.publisher.Mono.error

@Service
class HotelsService(
    @Autowired private val hotelsRepository: HotelsRepository,
) {
    /**
     * Find existing hotel by id, this method will switch to
     * an error of [Problem.NotFound] if hotel not found.
     *
     * @param hotelId hotel id to find
     * @return a Mono of [Hotels]
     */
    fun findExisting(hotelId: Long): Mono<Hotels> =
        hotelsRepository
            .findById(hotelId)
            .logOnNext(Level.DEBUG)
            .switchIfEmpty(error(Problem.NotFound("Hotel not found: $hotelId")))
}
