package com.katanox.api.domain.availability

data class AvailabilityResponse(
    val hotelId: Long,
    val roomId: Long,
    val price: Double,
    val currency: String,
)
