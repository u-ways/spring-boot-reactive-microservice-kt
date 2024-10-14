package com.katanox.api

import com.katanox.api.support.InfrastructureConfiguration
import com.katanox.api.support.IntegrationTest
import io.kotest.assertions.throwables.shouldNotThrowAny
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.SpringApplication
import org.springframework.boot.with

class ApplicationIT {
    @Nested
    inner class Smoke : IntegrationTest {
        @Test
        fun `application should start with no exceptions`() = shouldNotThrowAny { main(emptyArray()) }
    }

    companion object {
        /**
         * Spring Testcontainers integration support allows you to use them at development time.
         *
         * This approach allows developers to quickly start containers for the services that the
         * application depends on, removing the need to manually provision things like database servers.
         *
         * i.e. You can use the Gradle task:
         *
         * ```sh
         * ./gradlew bootTestRun --info
         * ```
         *
         * to start the application with the Testcontainers services running.
         *
         * See: https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#features.testcontainers.at-development-time
         */
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.from(Application::main)
                .with(InfrastructureConfiguration::class)
                .run(*args)
        }
    }
}
