package com.katanox.api.ext

import com.katanox.api.Application.Companion.LOGGER
import com.katanox.api.config.TraceabilityConfig.Companion.CORRELATION_CONTEXT_KEY
import com.katanox.api.config.TraceabilityConfig.Companion.CORRELATION_MDC_KEY
import org.slf4j.MDC
import org.slf4j.event.Level
import org.slf4j.event.Level.DEBUG
import org.slf4j.event.Level.ERROR
import org.slf4j.event.Level.INFO
import org.slf4j.event.Level.TRACE
import org.slf4j.event.Level.WARN
import org.springframework.http.HttpHeaders.USER_AGENT
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Signal
import java.time.Clock
import java.time.Instant
import kotlin.jvm.optionals.getOrNull

/**
 * A reactive mdc-aware logging operator that logs the signal's value.
 *
 * @param level The log level.
 * @param message The message to log.
 * @return The same [Mono] instance.
 */
internal fun <T> Mono<T>.logOnNext(
    level: Level = INFO,
    message: (T) -> String = { value -> value.toString() },
): Mono<T> =
    doOnEach { signal: Signal<T> ->
        mdcAwareLogger(signal, message, level)
    }

/**
 * A reactive mdc-aware logging operator that logs the signal's value.
 *
 * @param level The log level.
 * @param message The message to log.
 * @return The same [Flux] instance.
 */
internal fun <T> Flux<T>.logOnNext(
    level: Level = INFO,
    message: (T) -> String = { value -> value.toString() },
): Flux<T> =
    doOnEach { signal: Signal<T> ->
        mdcAwareLogger(signal, message, level)
    }

/**
 * A reactive mdc-aware request auditor operator that logs the request method
 * and path in the following format:
 *
 * ```
 * {REMOTE_IP} - - [{DATETIME}] "{METHOD} {URI} HTTP/1.1" {STATUS_CODE} - "{USER_AGENT}"
 * ```
 */
internal fun <T> Mono<T>.auditRequest(
    request: ServerHttpRequest,
    response: ServerHttpResponse,
    correlationId: String,
    clock: Clock = Clock.systemDefaultZone(),
) = doFinally {
    String
        .format(
            "%s - - [%s] \"%s %s HTTP/1.1\" %s - \"%s\"",
            request.remoteAddress?.address?.hostAddress ?: "-",
            Instant.now(clock),
                request.method,
            request.uri.path,
            response.statusCode?.value() ?: "000",
            request.headers.getFirst(USER_AGENT) ?: "Unknown",
        )
        .let { accessLog ->
            MDC
                .putCloseable(CORRELATION_MDC_KEY, correlationId)
                .use { LOGGER.info(accessLog) }
        }
}

/**
 * A reactive context-aware logging operator that logs the signal's value.
 *
 * The [CORRELATION_MDC_KEY] is fetched from the signal's context view and
 * temporarily set in the MDC context.
 *
 * @param level The log level.
 * @param message The message to log.
 * @return The same [Mono] instance.
 */
private fun <T> mdcAwareLogger(
    signal: Signal<T>,
    message: (T) -> String,
    level: Level,
) {
    if (signal.hasValue()) {
        signal
            .contextView
            .getOrEmpty<String>(CORRELATION_CONTEXT_KEY)
            .getOrNull()
            .takeUnless(String?::isNullOrEmpty)
            ?.let { correlationId ->
                // Temporarily sets a value in the MDC context, using the correlation ID fetched from the signal's context
                // view. This value is removed from the MDC context when the returned Closeable is closed. As such it will
                // not leak into other stages or threads.
                MDC
                    .putCloseable(CORRELATION_MDC_KEY, correlationId)
                    .use {
                        with(message.invoke(signal.get()!!)) {
                            when (level) {
                                TRACE -> LOGGER.trace(this)
                                DEBUG -> LOGGER.debug(this)
                                INFO -> LOGGER.info(this)
                                WARN -> LOGGER.warn(this)
                                ERROR -> LOGGER.error(this)
                            }
                        }
                    }
            }
    }
}
