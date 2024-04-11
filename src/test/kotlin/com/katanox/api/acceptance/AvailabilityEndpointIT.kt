package com.katanox.api.acceptance

import com.katanox.api.config.TraceabilityConfig.Companion.CORRELATION_HTTP_HEADER_KEY
import com.katanox.api.domain.availability.AvailabilityRequest
import com.katanox.api.domain.availability.AvailabilityResponse
import com.katanox.api.sql.enums.AppliedOn.FIRST_NIGHT
import com.katanox.api.sql.enums.ChargeType.ONCE
import com.katanox.api.sql.enums.ChargeType.PER_NIGHT
import com.katanox.api.support.E2EIntegrationTest
import com.katanox.api.support.domain.builder.ExtraChargeFlatBuilder.Companion.extraChargeFlat
import com.katanox.api.support.domain.builder.ExtraChargePercentageBuilder.Companion.extraChargePercentage
import com.katanox.api.support.domain.builder.PricesBuilder.Companion.price
import com.katanox.api.support.domain.builder.RoomsBuilder.Companion.room
import com.katanox.api.support.domain.matcher.ProblemResponseMatcher.Companion.problemDetailResponse
import com.katanox.api.support.domain.matcher.SearchResponseMatcher.Companion.searchResponse
import io.restassured.RestAssured.baseURI
import io.restassured.RestAssured.port
import io.restassured.http.ContentType.JSON
import java.time.LocalDate
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.OK

@TestMethodOrder(OrderAnnotation::class)
class AvailabilityEndpointIT : E2EIntegrationTest() {
    @ParameterizedTest(name = "given check-in ({0}) date is not at least one day before check-out ({1}) date, when request made, then 400 should be returned")
    @CsvSource(
        "2022-04-01, 2022-04-01",
        "2022-04-02, 2022-04-01",
    )
    @Order(1)
    fun `given check-in date is not at least one day before check-out date, when request made, then 400 should be returned`(
        checkIn: LocalDate,
        checkOut: LocalDate,
    ) {
        When {
            accept(JSON)
            contentType(JSON)
            header(CORRELATION_HTTP_HEADER_KEY, GIVEN_CORRELATION_ID)
            body(
                AvailabilityRequest(
                    HOTEL_A,
                    checkIn,
                    checkOut,
                )
            )
        } Executes {
            get(AVAILABILITY_ENDPOINT)
        } Then {
            statusCode(BAD_REQUEST.value())
            contentType(JSON)
            body(
                problemDetailResponse {
                    withType("$baseURI:$port/$AVAILABILITY_ENDPOINT")
                    withTitle(BAD_REQUEST.reasonPhrase)
                    withStatus(BAD_REQUEST.value())
                    withDetail("Check-in date should be at least one day before check-out date.")
                    withCorrelationId(GIVEN_CORRELATION_ID)
                }
            )
        }
    }

    @Test
    @Order(2)
    fun `given no matching hotel entity, when request made, then 404 should be returned`() {
        When {
            accept(JSON)
            contentType(JSON)
            header(CORRELATION_HTTP_HEADER_KEY, GIVEN_CORRELATION_ID)
            body(
                AvailabilityRequest(
                    HOTEL_A,
                    ROOM_A1_FIRST_DATE,
                    ROOM_A1_THIRD_DATE,
                )
            )
        } Executes {
            get(AVAILABILITY_ENDPOINT)
        } Then {
            statusCode(NOT_FOUND.value())
            contentType(JSON)
            body(
                problemDetailResponse {
                    withType("$baseURI:$port/$AVAILABILITY_ENDPOINT")
                    withTitle(NOT_FOUND.reasonPhrase)
                    withStatus(NOT_FOUND.value())
                    withDetail("Hotel not found: $HOTEL_A")
                    withCorrelationId(GIVEN_CORRELATION_ID)
                }
            )
        }
    }

    @Test
    @Order(3)
    fun `given no room entities, when request made, then 409 should be returned`() {
        Given {
            withHotelId(HOTEL_A)
        }
        When {
            accept(JSON)
            contentType(JSON)
            header(CORRELATION_HTTP_HEADER_KEY, GIVEN_CORRELATION_ID)
            body(
                AvailabilityRequest(
                    HOTEL_A,
                    ROOM_A1_FIRST_DATE,
                    ROOM_A1_THIRD_DATE,
                )
            )
        } Executes {
            get(AVAILABILITY_ENDPOINT)
        } Then {
            statusCode(CONFLICT.value())
            contentType(JSON)
            body(
                problemDetailResponse {
                    withType("$baseURI:$port/$AVAILABILITY_ENDPOINT")
                    withTitle(CONFLICT.reasonPhrase)
                    withStatus(CONFLICT.value())
                    withDetail("No rooms available for hotel: $HOTEL_A")
                    withCorrelationId(GIVEN_CORRELATION_ID)
                }
            )
        }
    }

    @Test
    @Order(4)
    fun `given no room pricing entities, when request made, then 409 should be returned`() {
        Given {
            withHotelId(HOTEL_A)
            withRooms(room().withRoomId(ROOM_A1))
        }
        When {
            accept(JSON)
            contentType(JSON)
            header(CORRELATION_HTTP_HEADER_KEY, GIVEN_CORRELATION_ID)
            body(
                AvailabilityRequest(
                    HOTEL_A,
                    ROOM_A1_FIRST_DATE,
                    ROOM_A1_THIRD_DATE,
                )
            )
        } Executes {
            get(AVAILABILITY_ENDPOINT)
        } Then {
            statusCode(CONFLICT.value())
            contentType(JSON)
            body(
                problemDetailResponse {
                    withType("$baseURI:$port/$AVAILABILITY_ENDPOINT")
                    withTitle(CONFLICT.reasonPhrase)
                    withStatus(CONFLICT.value())
                    withDetail("No room ($ROOM_A1) pricing available for nights between: $ROOM_A1_FIRST_DATE and $ROOM_A1_SECOND_DATE.")
                    withCorrelationId(GIVEN_CORRELATION_ID)
                }
            )
        }
    }

    @Test
    @Order(5)
    fun `given hotel with complete domain entities, when request made, then 200 should be returned`() {
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
                room()
                    .withRoomId(ROOM_A2)
                    .withPrice(
                        price()
                            .withPrice(ROOM_A2_FIRST_PRICE)
                            .withDate(ROOM_A2_FIRST_DATE),
                        price()
                            .withPrice(ROOM_A2_SECOND_PRICE)
                            .withDate(ROOM_A2_SECOND_DATE),
                        price()
                            .withPrice(ROOM_A2_THIRD_PRICE)
                            .withDate(ROOM_A2_THIRD_DATE),
                    ),
            )
            withExtraCharge(
                extraChargeFlat()
                    .withPrice(HOTEL_A_FLAT_PRICE_ONCE)
                    .withChargeType(ONCE),
                extraChargeFlat()
                    .withPrice(HOTEL_A_FLAT_PRICE_PER_NIGHT)
                    .withChargeType(PER_NIGHT)
            )
            withExtraCharge(
                extraChargePercentage()
                    .withPercentage(HOTEL_A_FLAT_PRICE_PERCENT)
                    .withAppliedOn(FIRST_NIGHT)
            )
        }
        When {
            accept(JSON)
            contentType(JSON)
            header(CORRELATION_HTTP_HEADER_KEY, GIVEN_CORRELATION_ID)
            body(
                AvailabilityRequest(
                    HOTEL_A,
                    ROOM_A1_FIRST_DATE,
                    ROOM_A1_THIRD_DATE,
                )
            )
        } Executes {
            get(AVAILABILITY_ENDPOINT)
        } Then {
            statusCode(OK.value())
            contentType(JSON)
            header(CORRELATION_HTTP_HEADER_KEY, "$GIVEN_CORRELATION_ID")
            body(
                searchResponse(
                    AvailabilityResponse(
                        HOTEL_A,
                        ROOM_A1,
                        EXPECTED_TOTAL_FOR_ROOM_A1,
                        HOTEL_A_CURRENCY.currencyCode,
                    ),
                    AvailabilityResponse(
                        HOTEL_A,
                        ROOM_A2,
                        EXPECTED_TOTAL_FOR_ROOM_A2,
                        HOTEL_A_CURRENCY.currencyCode,
                    ),
                )
            )
        }
    }

    companion object {
        private const val AVAILABILITY_ENDPOINT = "availability"
    }
}