package com.katanox.api.support.domain.matcher

import com.fasterxml.jackson.databind.ObjectMapper
import com.katanox.api.domain.booking.BookingDTO
import com.katanox.api.support.domain.builder.BookingDTOBuilder
import com.katanox.api.support.domain.builder.BookingDTOBuilder.Companion.bookingDTO
import io.kotest.assertions.fail
import io.kotest.matchers.shouldBe
import java.util.function.Supplier
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessageProperties

/**
 * A set of matchers for asserting on messages sent to the RabbitMQ broker.
 *
 * @property katanoxQueue A supplier for receiving messages from the main queue.
 * @property katanoxDlQueue A supplier for receiving messages from the dead-letter queue.
 */
class KatanoxQueueMatchers (
    val katanoxQueue: Supplier<Message?>,
    val katanoxDlQueue: Supplier<Message?>,
) {
    companion object {
        private val JSON_MAPPER = ObjectMapper().findAndRegisterModules()
    }

    fun Supplier<Message?>.shouldNotContainAnyMessages() = generateSequence(::get)
        .toList()
        .run { if (isNotEmpty()) fail("Expected queue to contain no messages, but received: $this") }

    infix fun Supplier<Message?>.shouldContainExactly(block: BookingDTOBuilder.() -> Unit): Message {
        val messages = generateSequence(::get).toList() // We only stop when null is encountered.
        if (messages.size != 1) fail("Expected queue to contain exactly one message, but received: $messages")
        else JSON_MAPPER
            .runCatching { readValue(String(messages.single().body), BookingDTO::class.java) }
            .onFailure { e -> fail("Failed to parse message body as a JSON object of ${BookingDTO::class.simpleName}: ${e.message}") }
            .getOrThrow()
            .shouldBe(bookingDTO().apply(block).build())
        return messages.single()
    }

    infix fun Message.withProperties(block: MessageProperties.() -> Unit): MessageProperties =
        messageProperties.apply(block)
}

