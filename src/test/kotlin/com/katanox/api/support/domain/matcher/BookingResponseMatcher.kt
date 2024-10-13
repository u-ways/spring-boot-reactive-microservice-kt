package com.katanox.api.support.domain.matcher

import com.katanox.api.domain.booking.BookingResponse
import io.restassured.path.json.JsonPath
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import java.util.UUID

class BookingResponseMatcher private constructor(
    private val expected: BookingResponse,
) : TypeSafeMatcher<String>() {
    override fun describeTo(description: Description) {
        expected.let(description::appendValue)
    }

    override fun matchesSafely(item: String): Boolean =
        JsonPath
            .from(item)
            .get<String>(BookingResponse::bookingId.name)
            .let(expected.bookingId.toString()::equals)

    companion object {
        @JvmStatic
        fun bookingResponse(bookingId: UUID) = BookingResponseMatcher(BookingResponse(bookingId))
    }
}
