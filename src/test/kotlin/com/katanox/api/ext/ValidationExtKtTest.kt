package com.katanox.api.ext

import com.katanox.api.domain.problem.Problem.InvalidInput
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class ValidationExtKtTest {
    @Nested
    inner class CheckForTest {
        @Test
        fun `checkFor throws problem when condition is not met`() {
            InvalidInput("should be odd").let { problem ->
                StepVerifier
                    .create(
                        Mono
                            .just(10)
                            .checkFor(problem) { this % 2 != 0 },
                    )
                    .expectErrorMatches {
                        it is InvalidInput && it.message == problem.message
                    }
                    .verify()
            }
        }

        @Test
        fun `checkFor passes through Mono when condition is met`() {
            StepVerifier
                .create(
                    Mono
                        .just(10)
                        .checkFor(InvalidInput("Should not be thrown")) { this % 2 == 0 },
                )
                .expectNext(10)
                .verifyComplete()
        }
    }
}
