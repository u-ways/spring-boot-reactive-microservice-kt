package com.katanox.api.support.domain.builder

import com.katanox.api.sql.tables.pojos.Prices
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.random.Random

class PricesBuilder {
    companion object {
        @JvmStatic
        fun price() = PricesBuilder()
    }

    fun withDate(date: LocalDate): PricesBuilder =
        this.apply {
            this.price = price.copy(date = date)
        }

    fun withPrice(price: BigDecimal): PricesBuilder =
        this.apply {
            this.price = this.price.copy(price = price)
        }

    private var price =
        Prices(
            roomId = Random.nextLong(),
            date = LocalDate.now(),
            price = BigDecimal.valueOf(Random.nextDouble(0.0, 100.0)),
        )

    fun build(roomId: Long): Prices = price.copy(roomId = roomId)
}
