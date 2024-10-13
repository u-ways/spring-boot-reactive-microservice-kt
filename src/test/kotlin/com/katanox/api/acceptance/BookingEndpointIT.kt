package com.katanox.api.acceptance

import com.katanox.api.config.TraceabilityConfig.Companion.CORRELATION_HTTP_HEADER_KEY
import com.katanox.api.domain.booking.BookingRequest
import com.katanox.api.domain.booking.BookingRequest.Guest
import com.katanox.api.domain.booking.BookingRequest.Payment
import com.katanox.api.sql.enums.AppliedOn.FIRST_NIGHT
import com.katanox.api.sql.enums.ChargeType.ONCE
import com.katanox.api.sql.enums.ChargeType.PER_NIGHT
import com.katanox.api.support.E2EIntegrationTest
import com.katanox.api.support.domain.builder.ExtraChargeFlatBuilder.Companion.extraChargeFlat
import com.katanox.api.support.domain.builder.ExtraChargePercentageBuilder.Companion.extraChargePercentage
import com.katanox.api.support.domain.builder.PricesBuilder.Companion.price
import com.katanox.api.support.domain.builder.RoomsBuilder.Companion.room
import com.katanox.api.support.domain.matcher.BookingResponseMatcher.Companion.bookingResponse
import com.katanox.api.support.domain.matcher.ProblemResponseMatcher.Companion.problemDetailResponse
import io.kotest.matchers.shouldBe
import io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON
import io.restassured.RestAssured.baseURI
import io.restassured.RestAssured.port
import io.restassured.http.ContentType.JSON
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.http.HttpStatus.ACCEPTED
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import java.time.LocalDate
import java.util.Currency

@TestMethodOrder(OrderAnnotation::class)
class BookingEndpointIT : E2EIntegrationTest() {
    @Test
    @Order(1)
    fun `given no hotel matching booking request, when request made, then 404 should be returned`() {
        When {
            accept(JSON)
            contentType(JSON)
            header(CORRELATION_HTTP_HEADER_KEY, GIVEN_CORRELATION_ID)
            body(
                BookingRequest(
                    HOTEL_B,
                    ROOM_B1,
                    CHECK_IN_DATE,
                    CHECK_OUT_DATE,
                    GUEST_BOOKING_PRICE,
                    GUEST_CURRENCY.currencyCode,
                    Guest(
                        GUEST_NAME,
                        GUEST_SURNAME,
                        GUEST_BIRTHDATE,
                    ),
                    Payment(
                        PAYMENT_CARD_HOLDER,
                        PAYMENT_CARD_NUMBER,
                        PAYMENT_CVV,
                        PAYMENT_EXPIRY_MONTH,
                        PAYMENT_EXPIRY_YEAR,
                    ),
                ),
            )
        } Executes {
            post(BOOKING_ENDPOINT)
        } Then {
            statusCode(NOT_FOUND.value())
            contentType(JSON)
            body(
                problemDetailResponse {
                    withType("$baseURI:$port/$BOOKING_ENDPOINT")
                    withTitle(NOT_FOUND.reasonPhrase)
                    withStatus(NOT_FOUND.value())
                    withDetail("Hotel not found: $HOTEL_B")
                    withCorrelationId(GIVEN_CORRELATION_ID)
                },
            )
        } And {
            katanoxQueue.shouldNotContainAnyMessages()
            katanoxDlQueue.shouldNotContainAnyMessages()
        }
    }

    @Test
    @Order(2)
    fun `given booking currency does not match hotel currency, when request made, then 400 should be returned`() {
        Given {
            withHotelId(HOTEL_A)
            withCurrency(HOTEL_A_CURRENCY)
            withVat(HOTEL_A_VAT)
            withRooms(
                room()
                    .withRoomId(ROOM_A1)
                    .withPrice(
                        price()
                            .withPrice(ROOM_A1_FIRST_PRICE)
                            .withDate(ROOM_A1_FIRST_DATE),
                        price()
                            .withPrice(ROOM_A1_SECOND_PRICE)
                            .withDate(ROOM_A1_SECOND_DATE),
                        price()
                            .withPrice(ROOM_A1_THIRD_PRICE)
                            .withDate(ROOM_A1_THIRD_DATE),
                    ),
            )
            withExtraCharge(
                extraChargeFlat()
                    .withPrice(HOTEL_A_FLAT_PRICE_ONCE)
                    .withChargeType(ONCE),
                extraChargeFlat()
                    .withPrice(HOTEL_A_FLAT_PRICE_PER_NIGHT)
                    .withChargeType(PER_NIGHT),
            )
            withExtraCharge(
                extraChargePercentage()
                    .withPercentage(HOTEL_A_FLAT_PRICE_PERCENT)
                    .withAppliedOn(FIRST_NIGHT),
            )
        }
        When {
            accept(JSON)
            contentType(JSON)
            header(CORRELATION_HTTP_HEADER_KEY, GIVEN_CORRELATION_ID)
            body(
                BookingRequest(
                    HOTEL_A,
                    ROOM_A1,
                    ROOM_A1_FIRST_DATE,
                    ROOM_A1_THIRD_DATE,
                    EXPECTED_TOTAL_FOR_ROOM_A1,
                    Currency.getAvailableCurrencies().minus(HOTEL_A_CURRENCY).random().currencyCode,
                    Guest(
                        GUEST_NAME,
                        GUEST_SURNAME,
                        GUEST_BIRTHDATE,
                    ),
                    Payment(
                        PAYMENT_CARD_HOLDER,
                        PAYMENT_CARD_NUMBER,
                        PAYMENT_CVV,
                        PAYMENT_EXPIRY_MONTH,
                        PAYMENT_EXPIRY_YEAR,
                    ),
                ),
            )
        } Executes {
            post(BOOKING_ENDPOINT)
        } Then {
            statusCode(BAD_REQUEST.value())
            contentType(JSON)
            body(
                problemDetailResponse {
                    withType("$baseURI:$port/$BOOKING_ENDPOINT")
                    withTitle(BAD_REQUEST.reasonPhrase)
                    withStatus(BAD_REQUEST.value())
                    withDetail("Currency does not match hotel requirements.")
                    withCorrelationId(GIVEN_CORRELATION_ID)
                },
            )
        } And {
            katanoxQueue.shouldNotContainAnyMessages()
            katanoxDlQueue.shouldNotContainAnyMessages()
        }
    }

    @Test
    @Order(3)
    fun `given booking price does not match availability quote for selected room, when request made, then 400 should be returned`() {
        Given {
            withHotelId(HOTEL_A)
            withCurrency(HOTEL_A_CURRENCY)
            withVat(HOTEL_A_VAT)
            withRooms(
                room()
                    .withRoomId(ROOM_A1)
                    .withPrice(
                        price()
                            .withPrice(ROOM_A1_FIRST_PRICE)
                            .withDate(ROOM_A1_FIRST_DATE),
                        price()
                            .withPrice(ROOM_A1_SECOND_PRICE)
                            .withDate(ROOM_A1_SECOND_DATE),
                        price()
                            .withPrice(ROOM_A1_THIRD_PRICE)
                            .withDate(ROOM_A1_THIRD_DATE),
                    ),
            )
        }
        When {
            accept(JSON)
            contentType(JSON)
            header(CORRELATION_HTTP_HEADER_KEY, GIVEN_CORRELATION_ID)
            body(
                BookingRequest(
                    HOTEL_A,
                    ROOM_A1,
                    ROOM_A1_FIRST_DATE,
                    ROOM_A1_THIRD_DATE,
                    CLIENT_TRYING_TO_BOOK_IT_FOR_FREE,
                    HOTEL_A_CURRENCY.currencyCode,
                    Guest(
                        GUEST_NAME,
                        GUEST_SURNAME,
                        GUEST_BIRTHDATE,
                    ),
                    Payment(
                        PAYMENT_CARD_HOLDER,
                        PAYMENT_CARD_NUMBER,
                        PAYMENT_CVV,
                        PAYMENT_EXPIRY_MONTH,
                        PAYMENT_EXPIRY_YEAR,
                    ),
                ),
            )
        } Executes {
            post(BOOKING_ENDPOINT)
        } Then {
            statusCode(BAD_REQUEST.value())
            contentType(JSON)
            body(
                problemDetailResponse {
                    withType("$baseURI:$port/$BOOKING_ENDPOINT")
                    withTitle(BAD_REQUEST.reasonPhrase)
                    withStatus(BAD_REQUEST.value())
                    withDetail("Price does not match the availability quote for room: $ROOM_A1")
                    withCorrelationId(GIVEN_CORRELATION_ID)
                },
            )
        } And {
            katanoxQueue.shouldNotContainAnyMessages()
            katanoxDlQueue.shouldNotContainAnyMessages()
        }
    }

    @Test
    @Order(4)
    fun `given valid booking details, when request made, then 202 should be returned`() {
        Given {
            withHotelId(HOTEL_A)
            withVat(HOTEL_A_VAT)
            withCurrency(HOTEL_A_CURRENCY)
            withRooms(
                room()
                    .withRoomId(ROOM_A1)
                    .withPrice(
                        price()
                            .withPrice(ROOM_A1_FIRST_PRICE)
                            .withDate(ROOM_A1_FIRST_DATE),
                        price()
                            .withPrice(ROOM_A1_SECOND_PRICE)
                            .withDate(ROOM_A1_SECOND_DATE),
                        price()
                            .withPrice(ROOM_A1_THIRD_PRICE)
                            .withDate(ROOM_A1_THIRD_DATE),
                    ),
            )
            withExtraCharge(
                extraChargeFlat()
                    .withPrice(HOTEL_A_FLAT_PRICE_ONCE)
                    .withChargeType(ONCE),
                extraChargeFlat()
                    .withPrice(HOTEL_A_FLAT_PRICE_PER_NIGHT)
                    .withChargeType(PER_NIGHT),
            )
            withExtraCharge(
                extraChargePercentage()
                    .withPercentage(HOTEL_A_FLAT_PRICE_PERCENT)
                    .withAppliedOn(FIRST_NIGHT),
            )
        }
        When {
            accept(JSON)
            contentType(JSON)
            header(CORRELATION_HTTP_HEADER_KEY, "$GIVEN_CORRELATION_ID")
            body(
                BookingRequest(
                    HOTEL_A,
                    ROOM_A1,
                    ROOM_A1_FIRST_DATE,
                    ROOM_A1_THIRD_DATE,
                    EXPECTED_TOTAL_FOR_ROOM_A1,
                    HOTEL_A_CURRENCY.currencyCode,
                    Guest(
                        GUEST_NAME,
                        GUEST_SURNAME,
                        GUEST_BIRTHDATE,
                    ),
                    Payment(
                        PAYMENT_CARD_HOLDER,
                        PAYMENT_CARD_NUMBER,
                        PAYMENT_CVV,
                        PAYMENT_EXPIRY_MONTH,
                        PAYMENT_EXPIRY_YEAR,
                    ),
                ),
            )
        } Executes {
            post(BOOKING_ENDPOINT)
        } Then {
            statusCode(ACCEPTED.value())
            contentType(JSON)
            body(bookingResponse(GIVEN_CORRELATION_ID))
        } And {
            katanoxQueue shouldContainExactly {
                withBookingId(GIVEN_CORRELATION_ID)
                withBookingRequest {
                    withHotelId(HOTEL_A)
                    withRoomId(ROOM_A1)
                    withCheckIn(ROOM_A1_FIRST_DATE)
                    withCheckOut(ROOM_A1_THIRD_DATE)
                    withPrice(EXPECTED_TOTAL_FOR_ROOM_A1)
                    withCurrency(HOTEL_A_CURRENCY)
                    withGuest(Guest(GUEST_NAME, GUEST_SURNAME, GUEST_BIRTHDATE))
                    withPayment(Payment(PAYMENT_CARD_HOLDER, PAYMENT_CARD_NUMBER, PAYMENT_CVV, PAYMENT_EXPIRY_MONTH, PAYMENT_EXPIRY_YEAR))
                }
            } withProperties {
                contentType shouldBe "$APPLICATION_JSON"
                correlationId shouldBe "$GIVEN_CORRELATION_ID"
            }

            katanoxDlQueue.shouldNotContainAnyMessages()
        }
    }

    companion object {
        private const val BOOKING_ENDPOINT = "booking"
        private const val CLIENT_TRYING_TO_BOOK_IT_FOR_FREE = 0.0

        internal val CHECK_IN_DATE = LocalDate.parse("2022-04-01")
        internal val CHECK_OUT_DATE = LocalDate.parse("2022-04-03")
    }
}
