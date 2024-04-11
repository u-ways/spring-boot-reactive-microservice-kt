package com.katanox.api.domain.extra.flat

import com.katanox.api.sql.tables.pojos.ExtraChargesFlat
import com.katanox.api.sql.tables.references.EXTRA_CHARGES_FLAT
import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
class ExtraChargesFlatRepository(
    @Autowired private val jooq: DSLContext,
) {
    fun findAllByHotelId(id: Long): Flux<ExtraChargesFlat> = Flux.from(jooq
        .selectFrom(EXTRA_CHARGES_FLAT)
        .where(EXTRA_CHARGES_FLAT.HOTEL_ID.eq(id)))
        .map { r -> r.into(ExtraChargesFlat::class.java) }
}
