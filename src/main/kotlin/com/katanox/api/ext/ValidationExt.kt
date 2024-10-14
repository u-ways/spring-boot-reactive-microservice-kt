package com.katanox.api.ext

import com.katanox.api.domain.problem.Problem
import reactor.core.publisher.Mono

/**
 * A reactive operator that errors out with
 * a [Problem] exception if the condition is not met.
 *
 * @param problem The problem to be thrown.
 * @param condition The condition to be checked.
 * @return The same [Mono] instance.
 */
internal fun <T> Mono<T>.checkFor(
    problem: Problem,
    condition: T.() -> Boolean,
): Mono<T> =
    this
        .filter(condition)
        .switchIfEmpty(Mono.error(problem))
