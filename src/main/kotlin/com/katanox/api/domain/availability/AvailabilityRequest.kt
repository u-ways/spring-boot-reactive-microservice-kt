package com.katanox.api.domain.availability

import java.time.LocalDate

data class AvailabilityRequest(
    val hotelId: Long,
    val checkIn: LocalDate,
    val checkOut: LocalDate,
)