package com.katanox.api.support.ext

import kotlin.random.Random.Default

/**
 * Generates a random alphanumeric string.
 *
 * @param prefix the prefix to be added to the random string.
 * @param suffix the suffix to be added to the random string.
 * @param length the length of the random string.
 *
 * @return the random alphanumeric string.
 */
fun Default.alphaNumeric(prefix: String? = null, suffix: String? = null, length: Int = 7) =
    (1..length)
        .joinToString("") { "${(('A'..'Z') + ('a'..'z') + ('0'..'9')).random()}" }
        .let { random -> prefix?.let { "$it-$random" } ?: random }
        .let { string -> suffix?.let { "$string-$it" } ?: string }
