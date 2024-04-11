package com.katanox.api.domain.availability

import com.katanox.api.domain.extra.flat.ExtraChargesFlatService
import com.katanox.api.domain.extra.percentage.ExtraChargesPercentageService
import com.katanox.api.domain.hotels.HotelsService
import com.katanox.api.domain.prices.PricesService
import com.katanox.api.domain.problem.Problem.InvalidInput
import com.katanox.api.domain.rooms.RoomsService
import com.katanox.api.ext.checkFor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2
import com.katanox.api.domain.booking.BookingService

/**
 * # Overview
 *
 * Service to search for available rooms in a hotel.
 *
 * - It calculates the price for each room based on the prices, extra charges and VAT.
 * - It caches the results for the same search request.
 *
 * Which is a simplified version of [Katanox's Availability API](https://api.katanox.com/v2/availability)
 * You can see the real API documentation [here](https://docs.katanox.com/reference/get-available-properties).
 *
 * # Implementation Details
 *
 * - The hotels table contains the timezone, VAT and currency information for each hotel.
 * - The rooms table contains rooms associated with each hotel, in an 1:N relationship.
 * - The prices table holds information about the price of a room for any given future night.
 * - You will need to apply VAT to the base price.
 * - Next, there are two extra charges tables:
 *   1. one which includes any flat extra charges that should be applied on top of the base price
 *   2. and one with percentage prices to be applied to the base price. (i.e. should not be applied to the VAT)
 *
 * When calculating the total price for a certain period, you need to take into account the extra charges of that room.
 *
 * ### Example Scenario:
 *
 * - Hotel: A,
 * - Check in: 01-04-2022,
 * - Check out: 03-04-2022 (2 nights)
 *
 * ##### Given the following hotel table:
 *
 * | id | name | address | rooms | timezone | vat | currency |
 * |----|------|---------|-------|----------|-----|----------|
 * | 1  | A    | X       | 2     | UTC+3    | 0.1 | EUR      |
 * | 2  | B    | Y       | 3     | UTC+2    | 0.2 | USD      |
 *
 * ##### and the rooms table:
 *
 * | id  | hotelId | name | description | quantity |
 * |-----|---------|------|-------------|----------|
 * | R1  | 1       | A1   | A1          | 2        |
 * | R2  | 1       | A2   | A2          | 3        |
 * | R3  | 2       | B1   | B1          | 1        |
 *
 * ##### and the prices table:
 *
 * | roomId | Date       | Quantity | price |
 * |--------|------------|----------|-------|
 * | R1     | 01-04-2022 | 2        | 103   |
 * | R1     | 02-04-2022 | 1        | 99    |
 * | R1     | 03-04-2022 | 2        | 110   |
 * | R2     | 01-04-2022 | 5        | 113   |
 * | R2     | 02-04-2022 | 6        | 109   |
 * | R2     | 03-04-2022 | 4        | 123   |
 * | R3     | 01-04-2022 | 1        | 150   |
 *
 * ##### and the extra flat charges table:
 *
 * | hotelId | Description  | chargeType  | price |
 * |---------|--------------|-------------|-------|
 * | 1       | Cleaning fee | ONCE        | 25    |
 * | 1       | Wi-Fi        | PER_NIGHT   | 5     |
 * | 2       | Cleaning fee | ONCE        | 50    |
 * | 2       | Safe         | PER_NIGHT   | 10    |
 *
 * ##### and the extra percentage charges table:
 *
 * | hotelId | Description  | appliedOn     | percentage |
 * |---------|--------------|---------------|------------|
 * | 1       | Staying fee  | FIRST_NIGHT   | 10         |
 * | 2       | Safety fee   | TOTAL_AMOUNT  | 15         |
 *
 * ##### Therefore, the total price for this scenario should be:
 *
 * ```
 *       ROOM PRICE  |    FLAT CHARGES    |    EXTRA CHARGES    |    VAT (ON BASE)       | TOTAL
 * R1 = (103 + 99 ) => 202 + (25 + 5 + 5) => 237 + (103 * 0.10) => 247.3 + (202 * 0.10) => 267.5
 * R2 = (113 + 109) => 222 + (25 + 5 + 5) => 257 + (113 * 0.10) => 268.3 + (222 * 0.10) => 290.5
 * ```
 *
 * It's important to note we only factor cost per night, i.e. 3 days = 2 nights.
 *
 * @see BookingService for how the availability service is used to book a room.
 */
@Service
class AvailabilityService(
    @Autowired private val hotelsService: HotelsService,
    @Autowired private val roomsService: RoomsService,
    @Autowired private val pricesService: PricesService,
    @Autowired private val extraChargesFlatService: ExtraChargesFlatService,
    @Autowired private val extraChargesPercentageService: ExtraChargesPercentageService,
) {
    companion object {
        private const val DO_NOT_COUNT_CHECKOUT = 1L
    }

    @Cacheable("availability.search")
    fun search(requestMono: Mono<AvailabilityRequest>): Flux<AvailabilityResponse> = requestMono
        .checkFor(InvalidInput("Check-in date should be at least one day before check-out date.")) {
            checkIn.toEpochDay() < checkOut.toEpochDay()
        }
        .flatMapMany { (hotelId, checkIn, checkOut) ->
            hotelsService
                .findExisting(hotelId)
                .flatMapMany { hotel ->
                    Mono.zip(
                        extraChargesFlatService.findAllByHotelId(hotelId),
                        extraChargesPercentageService.findAllByHotelId(hotelId)
                    ).flatMapMany { (flatCharges, percentageCharges) ->
                        roomsService
                            .findAllByHotelId(hotelId)
                            .flatMap { room ->
                                pricesService
                                    .findAllByRoomIdAndDateBetween(room.id!!, checkIn, checkOut.minusDays(DO_NOT_COUNT_CHECKOUT))
                            }
                            .filter { availability ->
                                availability.size == checkIn.datesUntil(checkOut).toList().size
                            }
                            .map { prices ->
                                AvailabilityResponse(
                                    hotelId = hotelId,
                                    roomId = prices.first().roomId,
                                    price = RoomsService.calculate(prices, flatCharges, percentageCharges, hotel.vat),
                                    currency = hotel.currency
                                )
                            }
                    }
                }
        }
        .cache()
}

