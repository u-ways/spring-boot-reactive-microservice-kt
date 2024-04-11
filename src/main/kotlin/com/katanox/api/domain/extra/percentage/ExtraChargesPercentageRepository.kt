package com.katanox.api.domain.extra.percentage

import com.katanox.api.sql.tables.pojos.ExtraChargesPercentage
import com.katanox.api.sql.tables.references.EXTRA_CHARGES_PERCENTAGE
import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.kotlin.core.publisher.toFlux

@Repository
class ExtraChargesPercentageRepository(
    @Autowired private val jooq: DSLContext,
) {
    fun findAllByHotelId(id: Long): Flux<ExtraChargesPercentage> = Flux.from(jooq
        .selectFrom(EXTRA_CHARGES_PERCENTAGE)
        .where(EXTRA_CHARGES_PERCENTAGE.HOTEL_ID.eq(id)))
        .map { r -> r.into(ExtraChargesPercentage::class.java) }
}
