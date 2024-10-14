package com.katanox.api.domain.rooms

import com.katanox.api.sql.tables.pojos.Rooms
import com.katanox.api.sql.tables.references.ROOMS
import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
class RoomsRepository(
    @Autowired private val jooq: DSLContext,
) {
    fun findAllByHotelId(id: Long): Flux<Rooms> =
        Flux.from(
            jooq
                .selectFrom(ROOMS)
                .where(ROOMS.HOTEL_ID.eq(id)),
        )
            .map { r -> r.into(Rooms::class.java) }
}
