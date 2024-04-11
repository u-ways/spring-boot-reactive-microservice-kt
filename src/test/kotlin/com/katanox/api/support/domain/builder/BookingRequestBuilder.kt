package com.katanox.api.support.domain.builder

import com.katanox.api.domain.booking.BookingRequest
import java.time.LocalDate
import java.util.Currency
import kotlin.properties.Delegates

class BookingRequestBuilder private constructor() {
    private var hotelId by Delegates.notNull<Long>()
    private var roomId by Delegates.notNull<Long>()
    private lateinit var checkIn: LocalDate
    private lateinit var checkOut: LocalDate
    private var price by Delegates.notNull<Double>()
    private lateinit var currency: Currency
    private lateinit var guest: BookingRequest.Guest
    private lateinit var payment: BookingRequest.Payment

    fun withHotelId(hotelId: Long) = apply {
        this.hotelId = hotelId
    }

    fun withRoomId(roomId: Long) = apply {
        this.roomId = roomId
    }

    fun withCheckIn(checkIn: LocalDate) = apply {
        this.checkIn = checkIn
    }

    fun withCheckOut(checkOut: LocalDate) = apply {
        this.checkOut = checkOut
    }

    fun withPrice(price: Double) = apply {
        this.price = price
    }

    fun withCurrency(currency: Currency) = apply {
        this.currency = currency
    }

    fun withGuest(guest: BookingRequest.Guest) = apply {
        this.guest = guest
    }

    fun withPayment(payment: BookingRequest.Payment) = apply {
        this.payment = payment
    }

    fun build() = BookingRequest(
        hotelId = hotelId,
        roomId = roomId,
        checkIn = checkIn,
        checkOut = checkOut,
        price = price,
        currency = currency.currencyCode,
        guest = guest,
        payment = payment,
    )

    companion object {
        @JvmStatic
        fun bookingRequest() = BookingRequestBuilder()
    }
}