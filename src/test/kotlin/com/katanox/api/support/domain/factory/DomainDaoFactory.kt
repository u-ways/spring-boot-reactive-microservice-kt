package com.katanox.api.support.domain.factory

import com.katanox.api.sql.tables.daos.ExtraChargesFlatDao
import com.katanox.api.sql.tables.daos.ExtraChargesPercentageDao
import com.katanox.api.sql.tables.daos.HotelsDao
import com.katanox.api.sql.tables.daos.PricesDao
import com.katanox.api.sql.tables.daos.RoomsDao
import com.katanox.api.support.domain.HotelDTO
import com.katanox.api.support.domain.RoomsDTO
import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class DomainDaoFactory(
    @Autowired @Qualifier("blockingJooq")
    private val jooq: DSLContext,
) {
    fun create(hotel: HotelDTO) =
        jooq.transaction { c ->
            HotelsDao(c).insert(hotel.hotel)
            RoomsDao(c).insert(hotel.rooms.map(RoomsDTO::room))
            PricesDao(c).insert(hotel.rooms.flatMap(RoomsDTO::prices))
            ExtraChargesFlatDao(c).insert(hotel.extraChargeFlat)
            ExtraChargesPercentageDao(c).insert(hotel.extraChargePercentage)
        }
}
