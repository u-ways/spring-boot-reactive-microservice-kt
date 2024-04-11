package com.katanox.api.ext

import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.util.UUID
import kotlin.reflect.full.findAnnotation
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import reactor.rabbitmq.OutboundMessage

class RabbitmqExtKtTest {
    @Nested
    inner class OutboundMessageTemplateTest {
        @Test
        fun `should be a SAM interface`() {
            OutboundMessageTemplate::class.isFun.shouldBeTrue()
        }

        @Test
        fun `should have a functional interface annotation`() {
            OutboundMessageTemplate::class.findAnnotation<FunctionalInterface>().shouldNotBeNull()
        }

        @Test
        fun `should return an OutboundMessage`() {
            OutboundMessageTemplate::class.java.declaredMethods[0]
                .returnType.shouldBe(OutboundMessage::class.java)
        }

        @Test
        fun `should expect a message of any type`() {
            OutboundMessageTemplate::class.java.declaredMethods[0].parameters[0].apply {
                name.shouldBe("message")
                type.shouldBe(Any::class.java)
            }
        }

        @Test
        fun `should expect a correlationId parameter`() {
            OutboundMessageTemplate::class.java.declaredMethods[0].parameters[1].apply {
                name.shouldBe("correlationId")
                type.shouldBe(UUID::class.java)
            }
        }
    }
}