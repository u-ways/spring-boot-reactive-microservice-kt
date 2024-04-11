package com.katanox.api.domain.rooms

import com.katanox.api.domain.extra.flat.ExtraChargesFlatService
import com.katanox.api.domain.extra.percentage.ExtraChargesPercentageService
import com.katanox.api.domain.prices.PricesService
import com.katanox.api.domain.problem.Problem
import com.katanox.api.ext.logOnNext
import com.katanox.api.sql.tables.pojos.ExtraChargesFlat
import com.katanox.api.sql.tables.pojos.ExtraChargesPercentage
import com.katanox.api.sql.tables.pojos.Prices
import com.katanox.api.sql.tables.pojos.Rooms
import java.math.BigDecimal
import org.slf4j.event.Level
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono.error

@Service
class RoomsService(
    @Autowired private val roomsRepository: RoomsRepository,
) {
    /**
     * Find all rooms for a hotel. This method will switch to an error
     * of [Problem.NotAvailable] if no rooms are available for the
     * subjected hotel.
     *
     * @param hotelId the hotel id
     * @return a Flux of [Rooms]
     */
    fun findAllByHotelId(hotelId: Long): Flux<Rooms> =
        roomsRepository
            .findAllByHotelId(hotelId)
            .logOnNext(Level.DEBUG)
            .switchIfEmpty(error(Problem.NotAvailable("No rooms available for hotel: $hotelId")))

    companion object {
        /**
         * Calculate the total price for a room.
         *
         * The algorithm is as follows:
         * 1. Calculate the base price via [PricesService.calculate].
         * 2. Calculate the flat charges via [ExtraChargesFlatService.calculate].
         * 3. Calculate the percentage charges via [ExtraChargesPercentageService.calculate].
         * 4. Calculate the VAT. (base price * VAT)
         * 5. Add all the calculated values.
         *
         * @param prices the list of prices
         * @param flatCharges the list of flat charges
         * @param percentageCharges the list of percentage charges
         * @param vat the VAT
         * @return the total price
         */
        @JvmStatic
        fun calculate(
            prices: List<Prices>,
            flatCharges: List<ExtraChargesFlat>,
            percentageCharges: List<ExtraChargesPercentage>,
            vat: BigDecimal,
        ): Double {
            val basePrice = PricesService.calculate(prices)
            val firstNight = prices.first()
            val flatChargesTotal = ExtraChargesFlatService.calculate(flatCharges, prices.count())
            val percentageChargesTotal = ExtraChargesPercentageService.calculate(percentageCharges, firstNight, basePrice)
            val vatChargesTotal = basePrice.multiply(vat)

            return basePrice.add(flatChargesTotal).add(percentageChargesTotal).add(vatChargesTotal).toDouble()
        }
    }
}