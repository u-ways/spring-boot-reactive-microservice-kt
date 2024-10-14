package com.katanox.api.support.domain.matcher

import com.katanox.api.config.TraceabilityConfig.Companion.CORRELATION_HTTP_HEADER_KEY
import io.restassured.path.json.JsonPath
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.properties.Delegates

class ProblemResponseMatcher private constructor() : TypeSafeMatcher<String>() {
    private lateinit var type: String
    private lateinit var title: String
    private var status by Delegates.notNull<Int>()
    private lateinit var detail: String
    private lateinit var correlationId: String
    private var timestamp: OffsetDateTime? = null

    override fun describeTo(description: Description) {
        description.appendText(
            """
            {
                "${ProblemResponseMatcher::type.name}": "$type",
                "${ProblemResponseMatcher::title.name}": "$title",
                "${ProblemResponseMatcher::status.name}": $status,
                "${ProblemResponseMatcher::detail.name}": "$detail",
                "$CORRELATION_HTTP_HEADER_KEY": "$correlationId",
                "${ProblemResponseMatcher::timestamp.name}": ${timestamp?.toDouble() ?: "IGNORED"},
            }
            """.trimIndent(),
        )
    }

    override fun matchesSafely(item: String): Boolean =
        JsonPath
            .from(item)
            ?.run {
                type == getString(ProblemResponseMatcher::type.name) &&
                    title == getString(ProblemResponseMatcher::title.name) &&
                    status == getInt(ProblemResponseMatcher::status.name) &&
                    detail == getString(ProblemResponseMatcher::detail.name) &&
                    correlationId == getString(CORRELATION_HTTP_HEADER_KEY) &&
                    timestamp?.toDouble()?.equals(getDouble(ProblemResponseMatcher::timestamp.name)) ?: true
            }
            ?: false

    fun withType(type: String) =
        apply {
            this.type = type
        }

    fun withTitle(title: String) =
        apply {
            this.title = title
        }

    fun withStatus(status: Int) =
        apply {
            this.status = status
        }

    fun withDetail(detail: String) =
        apply {
            this.detail = detail
        }

    fun withCorrelationId(correlationId: UUID) =
        apply {
            this.correlationId = correlationId.toString()
        }

    fun withTimestamp(timestamp: OffsetDateTime) =
        apply {
            this.timestamp = timestamp
        }

    /**
     * Convert the OffsetDateTime to a double representation.
     *
     * There is no Epoch double representation in OffsetDateTime, so we need to convert
     * it to a double representation by factoring in the nanoseconds.
     */
    private fun OffsetDateTime.toDouble(): Double {
        // Convert remaining nanoseconds to fractional seconds
        val fractionalSeconds = this.nano / 1_000_000_000.0
        return this.toEpochSecond() + fractionalSeconds
    }

    companion object {
        @JvmStatic
        internal fun problemDetailResponse(block: ProblemResponseMatcher.() -> Unit): ProblemResponseMatcher =
            ProblemResponseMatcher().apply(block)
    }
}
