package com.katanox.api.domain.extra.percentage

import com.katanox.api.ext.logOnNext
import com.katanox.api.sql.enums.AppliedOn
import com.katanox.api.sql.tables.pojos.ExtraChargesPercentage
import com.katanox.api.sql.tables.pojos.Prices
import java.math.BigDecimal
import org.slf4j.event.Level
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class ExtraChargesPercentageService(
    @Autowired private val extraChargesPercentageRepository: ExtraChargesPercentageRepository,
) {
    /**
     * Find all percentage extra charges by hotel id.
     *
     * @param hotelId the hotel id
     * @return a Flux of [ExtraChargesPercentage]
     */
    fun findAllByHotelId(hotelId: Long): Mono<List<ExtraChargesPercentage>> =
        extraChargesPercentageRepository
            .findAllByHotelId(hotelId)
            .logOnNext(Level.DEBUG)
            .collectList()

    companion object {
        private val PERCENTAGE_TO_DECIMAL_DIVISOR = 100.toBigDecimal()

        /**
         * Calculate the percentage extra charges as per [AppliedOn] requirements.
         *
         * @param percentageCharges the list of percentage charges
         * @param firstNight the first night price
         * @param basePrice the base price
         *
         * @return the total of percentage extra charges
         */
        @JvmStatic
        fun calculate(
            percentageCharges: List<ExtraChargesPercentage>,
            firstNight: Prices,
            basePrice: BigDecimal,
        ): BigDecimal = percentageCharges
            .fold(BigDecimal.ZERO) { acc, charge ->
                with(charge.percentage.divide(PERCENTAGE_TO_DECIMAL_DIVISOR)) {
                    when (charge.appliedOn) {
                        AppliedOn.FIRST_NIGHT -> acc + this.multiply(firstNight.price)
                        AppliedOn.TOTAL_AMOUNT -> acc + this.multiply(basePrice)
                    }
                }
            }
    }
}