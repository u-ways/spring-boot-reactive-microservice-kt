package com.katanox.api.domain.prices

import com.katanox.api.sql.tables.pojos.Prices
import com.katanox.api.sql.tables.references.PRICES
import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import java.time.LocalDate

@Repository
class PricesRepository(
    @Autowired private val jooq: DSLContext,
) {
    fun findAllByRoomIdAndDateBetween(
        id: Long,
        checkIn: LocalDate,
        minusDays: LocalDate?,
    ): Flux<Prices> =
        Flux.from(
            jooq
                .selectFrom(PRICES)
                .where(PRICES.ROOM_ID.eq(id))
                .and(PRICES.DATE.between(checkIn, minusDays)),
        )
            .map { r -> r.into(Prices::class.java) }
}
