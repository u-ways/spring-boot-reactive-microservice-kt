package com.katanox.api.support

import org.springframework.amqp.core.Message
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.amqp.RabbitProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.function.Supplier

/**
 * A blocking AMQP configuration for testing purposes.
 *
 * This MUST only be used within the test context. The current usecase
 * is for asserting on messages sent to the RabbitMQ broker.
 */
@Configuration
@EnableConfigurationProperties(RabbitProperties::class)
class BlockingAmqpConfig {
    @Bean
    fun rabbitTemplate(
        @Autowired connectionFactory: ConnectionFactory,
    ): RabbitTemplate = RabbitTemplate(connectionFactory)

    @Bean
    fun katanoxQueueReceiver(
        @Autowired rabbitTemplate: RabbitTemplate,
        @Autowired @Qualifier("katanoxQueue") katanoxQueue: Queue,
    ) = Supplier<Message?> {
        rabbitTemplate.receive(katanoxQueue.name)
    }

    @Bean
    fun katanoxDlQueueReceiver(
        @Autowired rabbitTemplate: RabbitTemplate,
        @Autowired @Qualifier("katanoxDlQueue") katanoxDlQueue: Queue,
    ) = Supplier<Message?> {
        rabbitTemplate.receive(katanoxDlQueue.name)
    }
}
