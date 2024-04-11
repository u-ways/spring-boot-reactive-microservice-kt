package com.katanox.api.domain.prices

import com.katanox.api.domain.problem.Problem
import com.katanox.api.ext.logOnNext
import com.katanox.api.sql.tables.pojos.Prices
import java.math.BigDecimal
import java.time.LocalDate
import org.slf4j.event.Level
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.publisher.Mono.error

@Service
class PricesService(
    @Autowired private val pricesRepository: PricesRepository,
) {
    /**
     * Find all prices for a room between two dates. This method will switch to
     * an error of [Problem.NotAvailable] if no prices are available for the
     * subjected room.
     *
     * @param roomId the room id
     * @param from the start date
     * @param to the end date
     * @return a Flux of [Prices]
     */
    fun findAllByRoomIdAndDateBetween(roomId: Long, from: LocalDate, to: LocalDate): Mono<List<Prices>> =
        pricesRepository
            .findAllByRoomIdAndDateBetween(roomId, from, to)
            .logOnNext(Level.DEBUG)
            .switchIfEmpty(error(Problem.NotAvailable("No room ($roomId) pricing available for nights between: $from and $to.")))
            .collectList()

    companion object {
        /**
         * Calculate the total price of a list of prices.
         * Also known as the "base price".
         *
         * @param prices the list of prices
         * @return the total price (base price)
         */
        @JvmStatic
        fun calculate(
            prices: List<Prices>,
        ): BigDecimal = prices.map(Prices::price).reduce(BigDecimal::add)
    }
}