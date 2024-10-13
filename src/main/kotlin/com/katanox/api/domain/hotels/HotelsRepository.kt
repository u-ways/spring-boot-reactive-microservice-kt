package com.katanox.api.domain.hotels

import com.katanox.api.sql.tables.pojos.Hotels
import com.katanox.api.sql.tables.references.HOTELS
import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
class HotelsRepository(
    @Autowired private val jooq: DSLContext,
) {
    fun findById(id: Long): Mono<Hotels> =
        Mono.from(
            jooq
                .select()
                .from(HOTELS)
                .where(HOTELS.ID.eq(id)),
        )
            .map { r -> r.into(Hotels::class.java) }
}
