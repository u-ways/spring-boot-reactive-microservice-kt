package com.katanox.api.support.domain.builder

import com.katanox.api.sql.tables.pojos.Rooms
import com.katanox.api.support.domain.RoomsDTO
import com.katanox.api.support.ext.alphaNumeric
import kotlin.random.Random

class RoomsBuilder private constructor() {
    companion object {
        @JvmStatic
        fun room() = RoomsBuilder()
    }

    private var room =
        Rooms(
            id = Random.nextLong(0, 10000),
            hotelId = Random.nextLong(0, 10000),
            name = Random.alphaNumeric("room-name"),
            description = Random.alphaNumeric("room-description"),
            quantity = Random.nextInt(0, 10),
        )

    private var price: List<PricesBuilder> = emptyList()

    fun withRoomId(id: Long): RoomsBuilder =
        this.apply {
            this.room = room.copy(id = id)
        }

    fun withPrice(vararg price: PricesBuilder): RoomsBuilder =
        this.apply {
            this.price = price.toList()
        }

    fun build(hotelId: Long): RoomsDTO =
        RoomsDTO(
            room = room.copy(hotelId = hotelId),
            prices = price.map { it.build(room.id!!) },
        )
}
