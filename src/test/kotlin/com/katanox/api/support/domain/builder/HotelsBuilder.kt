package com.katanox.api.support.domain.builder

import com.katanox.api.sql.tables.pojos.Hotels
import com.katanox.api.support.domain.HotelDTO
import com.katanox.api.support.ext.alphaNumeric
import java.math.BigDecimal
import java.util.Currency
import java.util.TimeZone
import kotlin.random.Random

class HotelsBuilder private constructor() {
    companion object {
        @JvmStatic
        fun hotel() = HotelsBuilder()
    }

    private var hotel =
        Hotels(
            id = Random.nextLong(0, 10000),
            name = Random.alphaNumeric("hotel-name"),
            address = Random.alphaNumeric("hotel-address"),
            timezone = TimeZone.getAvailableIDs().random(),
            vat = BigDecimal.valueOf(Random.nextDouble(1.0, 100.0)),
            currency = Currency.getAvailableCurrencies().random().currencyCode,
        )

    private var rooms: List<RoomsBuilder> = emptyList()
    private var extraChargeFlat: List<ExtraChargeFlatBuilder> = emptyList()
    private var extraChargePercentage: List<ExtraChargePercentageBuilder> = emptyList()

    fun withHotelId(id: Long): HotelsBuilder =
        this.apply {
            this.hotel = hotel.copy(id = id)
        }

    fun withVat(vat: BigDecimal): HotelsBuilder =
        this.apply {
            this.hotel = hotel.copy(vat = vat)
        }

    fun withCurrency(currency: Currency): HotelsBuilder =
        this.apply {
            this.hotel = hotel.copy(currency = currency.currencyCode)
        }

    fun withRooms(vararg rooms: RoomsBuilder): HotelsBuilder =
        this.apply {
            this.rooms = rooms.toList()
        }

    fun withExtraCharge(vararg extraChargeFlat: ExtraChargeFlatBuilder): HotelsBuilder =
        this.apply {
            this.extraChargeFlat = extraChargeFlat.toList()
        }

    fun withExtraCharge(vararg extraChargePercentage: ExtraChargePercentageBuilder): HotelsBuilder =
        this.apply {
            this.extraChargePercentage = extraChargePercentage.toList()
        }

    fun build(): HotelDTO =
        HotelDTO(
            hotel = hotel,
            rooms = rooms.map { it.build(hotel.id!!) },
            extraChargeFlat = extraChargeFlat.map { it.build(hotel.id!!) },
            extraChargePercentage = extraChargePercentage.map { it.build(hotel.id!!) },
        )
}
