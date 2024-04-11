package com.katanox.api.domain.booking

import java.util.UUID

data class BookingDTO(
    val bookingId: UUID,
    val bookingRequest: BookingRequest,
)