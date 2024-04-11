package com.katanox.api.support.domain.builder

import com.katanox.api.domain.booking.BookingDTO
import com.katanox.api.domain.booking.BookingRequest
import java.util.UUID

class BookingDTOBuilder private constructor() {
    private lateinit var bookingId: UUID
    private lateinit var request: BookingRequest

    fun withBookingId(bookingId: UUID) = apply {
        this.bookingId = bookingId
    }

    fun withBookingRequest(request: BookingRequestBuilder.() -> Unit) = apply {
        this.request = BookingRequestBuilder.bookingRequest().apply(request).build()
    }

    fun build() = BookingDTO(bookingId, request)

    companion object {
        @JvmStatic
        fun bookingDTO() = BookingDTOBuilder()
    }
}