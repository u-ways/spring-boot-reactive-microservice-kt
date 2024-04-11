package com.katanox.api.support.domain.matcher

import com.katanox.api.domain.availability.AvailabilityResponse
import io.restassured.path.json.JsonPath
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

class SearchResponseMatcher private constructor(
    private val expected: List<AvailabilityResponse>,
) : TypeSafeMatcher<String>() {
    override fun describeTo(description: Description) {
        expected.let(description::appendValue)
    }

    override fun matchesSafely(item: String) = JsonPath
        .from(item)
        .getList<Map<String, Any>>("$")
        // Ensure the list has the same size as the expected list.
        // This is also critical to ensure below comparison works as expected.
        .takeIf { it.size == expected.size }
        ?.let { actualList ->
            expected.all {
                // We don't care about the order of the elements in the list. (i.e. no ordering requirement)
                // For as long as the expected elements are present in the actual list, we're good.
                actualList.any { actual ->
                    "${actual[AvailabilityResponse::hotelId.name]}".toLong() == it.hotelId &&
                        "${actual[AvailabilityResponse::roomId.name]}".toLong() == it.roomId &&
                        "${actual[AvailabilityResponse::price.name]}".toDouble() == it.price &&
                        "${actual[AvailabilityResponse::currency.name]}" == it.currency
                }
            }
        }
        ?: false

    companion object {
        @JvmStatic
        fun searchResponse(vararg availabilityResponse: AvailabilityResponse) =
            SearchResponseMatcher(availabilityResponse.toList())
    }
}