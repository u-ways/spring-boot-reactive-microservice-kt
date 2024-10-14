package com.katanox.api.support.domain

import com.katanox.api.sql.tables.pojos.ExtraChargesFlat
import com.katanox.api.sql.tables.pojos.ExtraChargesPercentage
import com.katanox.api.sql.tables.pojos.Hotels

data class HotelDTO(
    val hotel: Hotels,
    val rooms: List<RoomsDTO>,
    val extraChargeFlat: List<ExtraChargesFlat>,
    val extraChargePercentage: List<ExtraChargesPercentage>,
)
