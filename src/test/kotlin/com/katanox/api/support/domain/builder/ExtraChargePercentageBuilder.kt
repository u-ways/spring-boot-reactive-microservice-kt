package com.katanox.api.support.domain.builder

import com.katanox.api.sql.enums.AppliedOn
import com.katanox.api.sql.tables.pojos.ExtraChargesPercentage
import com.katanox.api.support.ext.alphaNumeric
import java.math.BigDecimal
import kotlin.random.Random

class ExtraChargePercentageBuilder private constructor() {
    companion object {
        @JvmStatic
        fun extraChargePercentage() = ExtraChargePercentageBuilder()
    }

    private var extraChargePercentage = ExtraChargesPercentage(
        id = Random.nextLong(0, 10000),
        hotelId = Random.nextLong(0, 10000),
        description = Random.alphaNumeric("extra-charge-percentage-description"),
        appliedOn = AppliedOn.entries.toTypedArray().random(),
        percentage = BigDecimal.valueOf(Random.nextDouble(0.0, 100.0)),
    )

    fun withAppliedOn(appliedOn: AppliedOn): ExtraChargePercentageBuilder = this.apply {
        this.extraChargePercentage = extraChargePercentage.copy(appliedOn = appliedOn)
    }

    fun withPercentage(percentage: BigDecimal): ExtraChargePercentageBuilder = this.apply {
        this.extraChargePercentage = extraChargePercentage.copy(percentage = percentage)
    }

    fun build(hotelId: Long): ExtraChargesPercentage = extraChargePercentage.copy(hotelId = hotelId)
}