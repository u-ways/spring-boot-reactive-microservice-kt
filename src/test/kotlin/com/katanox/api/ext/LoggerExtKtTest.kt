package com.katanox.api.ext

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import com.katanox.api.Application.Companion.LOGGER
import com.katanox.api.config.TraceabilityConfig.Companion.CORRELATION_CONTEXT_KEY
import com.katanox.api.config.TraceabilityConfig.Companion.CORRELATION_MDC_KEY
import com.katanox.api.support.logging.LogsCollector
import com.katanox.api.support.logging.LogsCollectorExtension
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldMatch
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.MDC
import org.slf4j.event.Level.DEBUG
import org.slf4j.event.Level.ERROR
import org.slf4j.event.Level.INFO
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.USER_AGENT
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.test.StepVerifier
import java.net.InetSocketAddress
import java.time.Clock
import java.time.Instant
import java.util.UUID

class LoggerExtKtTest {
    private lateinit var streamOfData: Mono<String>

    @BeforeEach
    fun setUp() {
        streamOfData = Mono.just("stream of data...")
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Nested
    @ExtendWith(LogsCollectorExtension::class)
    inner class AuditRequestTest {
        private val infoCollector = LogsCollector(LOGGER as Logger, Level.INFO)

        private lateinit var request: ServerHttpRequest
        private lateinit var response: ServerHttpResponse

        @BeforeEach
        fun setupResponseRequest() {
            request = mockk()
            response = mockk()

            every { request.method } returns HttpMethod.GET
            every { request.uri } returns java.net.URI.create("http://localhost/test/path")
            every { request.headers } returns HttpHeaders().apply { add("User-Agent", "TestAgent") }
            every { request.remoteAddress } returns InetSocketAddress("123.123.123.123", 8080)
            every { response.statusCode } returns HttpStatus.OK
        }

        @Test
        fun `should not complete the stream`() {
            StepVerifier
                .create(streamOfData.auditRequest(request, response, GIVEN_CORRELATION_ID))
                .expectNext("stream of data...")
                .verifyComplete()
        }

        @Test
        fun `should log audit at info level`() {
            streamOfData
                .auditRequest(request, response, GIVEN_CORRELATION_ID)
                .block()

            infoCollector.logs().shouldBeSingleton()
        }

        @Test
        fun `should log correct format`() {
            streamOfData
                .auditRequest(request, response, GIVEN_CORRELATION_ID)
                .block()

            infoCollector.logs()
                .shouldBeSingleton()
                .first()
                .shouldMatch(Regex("""\S+ - - \[.*?] ".*? HTTP/1\.1" \d{3} - ".*?""""))
        }

        @Test
        fun `should template the correct values`() {
            streamOfData
                .auditRequest(
                    request,
                    response,
                    GIVEN_CORRELATION_ID,
                    Clock.fixed(Instant.EPOCH, Clock.systemDefaultZone().zone),
                )
                .block()

            val expectedAccessLog =
                String
                    .format(
                        "%s - - [%s] \"%s %s HTTP/1.1\" %s - \"%s\"",
                        request.remoteAddress?.address?.hostAddress ?: "-",
                        Instant.EPOCH,
                        request.method,
                        request.uri.path,
                        response.statusCode?.value() ?: "000",
                        request.headers.getFirst(USER_AGENT) ?: "Unknown",
                    )

            infoCollector.logs()
                .shouldBeSingleton()
                .first()
                .shouldBe(expectedAccessLog)
        }
    }

    @Nested
    inner class LogOnNextTest {
        @Nested
        @ExtendWith(LogsCollectorExtension::class)
        inner class Mono {
            private val debugCollector = LogsCollector(LOGGER as Logger, Level.DEBUG)

            @Test
            fun `should not complete the stream`() {
                StepVerifier
                    .create(streamOfData.logOnNext())
                    .expectNext("stream of data...")
                    .verifyComplete()
            }

            @Test
            fun `should collect correlation id value from context view`() {
                mockkStatic(MDC::class) {
                    streamOfData
                        .logOnNext(INFO) { "logged: $it" }
                        .contextWrite { ctx -> ctx.put(CORRELATION_CONTEXT_KEY, GIVEN_CORRELATION_ID) }
                        .block()

                    verify { MDC.putCloseable(CORRELATION_MDC_KEY, GIVEN_CORRELATION_ID) }
                }
            }

            @Test
            fun `should log at info level`() {
                streamOfData
                    .logOnNext(DEBUG) { "logged: $it" }
                    .contextWrite { ctx -> ctx.put(CORRELATION_CONTEXT_KEY, GIVEN_CORRELATION_ID) }
                    .block()

                debugCollector.logs()
                    .shouldBeSingleton()
                    .shouldContain("logged: stream of data...")
            }
        }

        @Nested
        @ExtendWith(LogsCollectorExtension::class)
        inner class Flux {
            private val errorCollector = LogsCollector(LOGGER as Logger, Level.ERROR)

            @Test
            fun `should not complete the stream`() {
                StepVerifier
                    .create(streamOfData.toFlux().logOnNext())
                    .expectNext("stream of data...")
                    .verifyComplete()
            }

            @Test
            fun `should collect correlation id value from context view`() {
                mockkStatic(MDC::class) {
                    streamOfData
                        .toFlux()
                        .logOnNext(INFO) { "logged: $it" }
                        .contextWrite { ctx -> ctx.put(CORRELATION_CONTEXT_KEY, GIVEN_CORRELATION_ID) }
                        .blockLast()

                    verify { MDC.putCloseable(CORRELATION_MDC_KEY, GIVEN_CORRELATION_ID) }
                }
            }

            @Test
            fun `should log at info level`() {
                streamOfData
                    .toFlux()
                    .logOnNext(ERROR) { "logged: $it" }
                    .contextWrite { ctx -> ctx.put(CORRELATION_CONTEXT_KEY, GIVEN_CORRELATION_ID) }
                    .blockLast()

                errorCollector.logs()
                    .shouldBeSingleton()
                    .shouldContain("logged: stream of data...")
            }
        }
    }

    companion object {
        private val GIVEN_CORRELATION_ID = UUID.randomUUID().toString()
    }
}
