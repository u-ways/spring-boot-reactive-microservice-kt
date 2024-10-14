package com.katanox.api.support.domain

import com.katanox.api.sql.tables.pojos.Prices
import com.katanox.api.sql.tables.pojos.Rooms

data class RoomsDTO(
    val room: Rooms,
    val prices: List<Prices>,
)
