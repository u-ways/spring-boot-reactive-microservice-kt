package com.katanox.api.domain.problem

import java.time.OffsetDateTime

data class ProblemResponse(
    val correlationId: String,
    val timestamp: OffsetDateTime,
    val status: Int,
    val message: String,
)
