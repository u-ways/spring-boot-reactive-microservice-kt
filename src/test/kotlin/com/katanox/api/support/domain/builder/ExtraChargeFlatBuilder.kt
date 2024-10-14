package com.katanox.api.support.domain.builder

import com.katanox.api.sql.enums.ChargeType
import com.katanox.api.sql.tables.pojos.ExtraChargesFlat
import com.katanox.api.support.ext.alphaNumeric
import java.math.BigDecimal
import kotlin.random.Random

class ExtraChargeFlatBuilder private constructor() {
    companion object {
        @JvmStatic
        fun extraChargeFlat() = ExtraChargeFlatBuilder()
    }

    private var extraChargeFlat =
        ExtraChargesFlat(
            id = Random.nextLong(0, 10000),
            hotelId = Random.nextLong(0, 10000),
            description = Random.alphaNumeric("extra-charge-flat-description"),
            chargeType = ChargeType.entries.toTypedArray().random(),
            price = BigDecimal.valueOf(Random.nextDouble(0.0, 200.0)),
        )

    fun withChargeType(chargeType: ChargeType): ExtraChargeFlatBuilder =
        this.apply {
            this.extraChargeFlat = extraChargeFlat.copy(chargeType = chargeType)
        }

    fun withPrice(price: BigDecimal): ExtraChargeFlatBuilder =
        this.apply {
            this.extraChargeFlat = extraChargeFlat.copy(price = price)
        }

    fun build(hotelId: Long): ExtraChargesFlat = extraChargeFlat.copy(hotelId = hotelId)
}
