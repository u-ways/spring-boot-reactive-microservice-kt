package com.katanox.api.domain.extra.flat

import com.katanox.api.ext.logOnNext
import com.katanox.api.sql.enums.ChargeType
import com.katanox.api.sql.tables.pojos.ExtraChargesFlat
import org.slf4j.event.Level
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.math.BigDecimal

@Service
class ExtraChargesFlatService(
    @Autowired private val extraChargesFlatRepository: ExtraChargesFlatRepository,
) {
    /**
     * Find all flat extra charges by hotel id.
     *
     * @param hotelId the hotel id
     * @return a Flux of [ExtraChargesFlat]
     */
    fun findAllByHotelId(hotelId: Long): Mono<List<ExtraChargesFlat>> =
        extraChargesFlatRepository
            .findAllByHotelId(hotelId)
            .logOnNext(Level.DEBUG)
            .collectList()

    companion object {
        /**
         * Calculate the flat extra charges as per [ChargeType] requirements.
         *
         * @param flatCharges the list of flat charges
         * @param nightsCount the number of nights
         *
         * @return the total of flat extra charges
         */
        @JvmStatic
        fun calculate(
            flatCharges: List<ExtraChargesFlat>,
            nightsCount: Int,
        ): BigDecimal =
            flatCharges.fold(BigDecimal.ZERO) { acc, charge ->
                acc + (
                    when (charge.chargeType) {
                        ChargeType.ONCE -> charge.price
                        ChargeType.PER_NIGHT -> charge.price.multiply(nightsCount.toBigDecimal())
                    }
                )
            }
    }
}
