package com.katanox.api.domain.booking

import com.fasterxml.jackson.annotation.JsonAlias
import java.time.LocalDate

data class BookingRequest(
    val hotelId: Long,
    val roomId: Long,
    val checkIn: LocalDate,
    val checkOut: LocalDate,
    val price: Double,
    val currency: String,
    val guest: Guest,
    val payment: Payment,
) {
    data class Guest(
        val name: String,
        val surname: String,
        val birthdate: LocalDate,
    ) {
        override fun toString(): String =
            "Guest(name='$OBSCURED', surname='$OBSCURED', birthdate=$OBSCURED)"
    }

    data class Payment(
        @JsonAlias("card_holder") val cardHolder: String,
        @JsonAlias("card_number") val cardNumber: String,
        val cvv: String,
        @JsonAlias("expiry_month") val expiryMonth: String,
        @JsonAlias("expiry_year") val expiryYear: String,
    ) {
        override fun toString(): String =
            "Payment(cardHolder='$OBSCURED', cardNumber='$OBSCURED', cvv='$OBSCURED', expiryMonth='$OBSCURED', expiryYear='$OBSCURED')"
    }

    companion object {
        const val OBSCURED = "****"
    }
}