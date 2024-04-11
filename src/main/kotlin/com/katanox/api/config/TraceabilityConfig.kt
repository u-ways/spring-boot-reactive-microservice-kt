package com.katanox.api.config

import com.katanox.api.ext.auditRequest
import java.util.UUID
import org.slf4j.MDC
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered.HIGHEST_PRECEDENCE
import org.springframework.core.annotation.Order
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

/**
 * A filter that sets the correlation ID in the reactor context and the MDC context.
 *
 * The correlation ID is used to track a request across multiple services. It is set
 * in the request headers and passed along to the downstream services. If it is not
 * present in the request headers, a new correlation ID is generated.
 *
 * The correlation ID is usually generated as a UUID.
 */
@Configuration
@Order(HIGHEST_PRECEDENCE)
class TraceabilityConfig : WebFilter {
    companion object {
        /** Used to store the correlation ID in the MDC context for the logging framework. */
        internal const val CORRELATION_MDC_KEY: String = "CORRELATION_ID"

        /** Used to store the correlation ID in the reactor context. */
        internal const val CORRELATION_CONTEXT_KEY: String = "CORRELATION_CONTEXT"

        /** The key used to fetch the correlation ID from the request headers. */
        internal const val CORRELATION_HTTP_HEADER_KEY: String = "x-correlation-id"

        /** The default correlation ID provider. */
        internal val CORRELATION_ID_PROVIDER: () -> String = { UUID.randomUUID().toString() }
    }

    override fun filter(exchange: ServerWebExchange, upstreamChain: WebFilterChain): Mono<Void> {
        val correlationId = exchange.request
            .headers[CORRELATION_HTTP_HEADER_KEY]
            .takeUnless { h -> h.isNullOrEmpty() }
            ?.first()
            ?: CORRELATION_ID_PROVIDER.invoke()

        return upstreamChain
            .filter(exchange)
            // It's important to note that context is propagated downstream to upstream, through the subscription.
            // See: [Reactor - 9.8.1. The Context API](https://projectreactor.io/docs/core/release/reference/#context.api)
            .contextWrite { ctx ->
                // Share the correlation ID across the exchange context.
                exchange.attributes[CORRELATION_CONTEXT_KEY] = correlationId
                // Ensures that the correlation ID is set in the response headers just before the response is committed.
                exchange.response.beforeCommit {
                    Mono.fromRunnable { exchange.response.headers.set(CORRELATION_HTTP_HEADER_KEY, correlationId) }
                }
                // Share the correlation ID across the reactor context.
                ctx.put(CORRELATION_CONTEXT_KEY, correlationId)
            }
            .auditRequest(exchange.request, exchange.response, correlationId)
            .doFinally { MDC.clear() }
    }
}


