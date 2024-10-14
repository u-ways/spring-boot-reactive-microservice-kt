package com.katanox.api.domain.problem

import java.time.OffsetDateTime

internal sealed class Problem(
    override val message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause) {
    val timestamp: OffsetDateTime = OffsetDateTime.now()

    internal class NotFound(message: String, cause: Throwable? = null) : Problem(message, cause)

    internal class NotAvailable(message: String, cause: Throwable? = null) : Problem(message, cause)

    internal class InvalidInput(message: String, cause: Throwable? = null) : Problem(message, cause)

    internal class BadGateway(message: String, cause: Throwable? = null) : Problem(message, cause)
}
