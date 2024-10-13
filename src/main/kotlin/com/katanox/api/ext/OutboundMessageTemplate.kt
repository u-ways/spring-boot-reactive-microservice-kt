package com.katanox.api.ext

import java.util.UUID
import reactor.rabbitmq.OutboundMessage

/**
 * A functional interface that prepares an [OutboundMessage] from a message and a correlation ID.
 * This is useful for defining a template for sending messages to RabbitMQ.
 */
@FunctionalInterface
fun interface OutboundMessageTemplate {
    fun prepare(message: Any, correlationId: UUID): OutboundMessage
}