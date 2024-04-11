package com.katanox.api.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.katanox.api.ext.OutboundMessageTemplate
import com.rabbitmq.client.AMQP.BasicProperties
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import java.util.Date
import kotlin.text.Charsets.UTF_8
import org.springframework.amqp.core.AmqpAdmin
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.DirectExchange
import org.springframework.amqp.core.ExchangeBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.QueueBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.amqp.RabbitProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.rabbitmq.OutboundMessage
import reactor.rabbitmq.RabbitFlux
import reactor.rabbitmq.Sender
import reactor.rabbitmq.SenderOptions


@Configuration
class RabbitMQConfig(
    @Autowired private val admin: AmqpAdmin,
    @Value("\${katanox.rabbitmq.exchange}") private val exchange: String,
    @Value("\${katanox.rabbitmq.routingkey}") private val routingKey: String,
    @Value("\${katanox.rabbitmq.queue}") private val queueName: String,
    @Value("\${katanox.rabbitmq.exchange.dl}") private val dlExchange: String,
    @Value("\${katanox.rabbitmq.routingkey.dl}") private val dlRoutingKey: String,
    @Value("\${katanox.rabbitmq.queue.dl}") private val dlQueueName: String,
) {
    companion object {
        private const val SET_AS_DURABLE = true
        private const val REACTIVE_CONNECTION_NAME = "reactive-rabbit-connection"
        private const val STORED_ON_DISK_PERSISTENCE = 2
        private val CURRENT_TIMESTAMP_SUPPLIER = { Date() }
    }

    @Bean
    fun katanoxDlQueue(): Queue = QueueBuilder
        .durable(dlQueueName)
        .build()
        .also(admin::declareQueue)

    @Bean
    fun katanoxDlExchange(): DirectExchange = ExchangeBuilder
        .directExchange(dlExchange)
        .durable(SET_AS_DURABLE)
        .build<DirectExchange>()
        .also(admin::declareExchange)


    @Bean
    fun katanoxDlBinding(
        @Autowired katanoxDlQueue: Queue,
        @Autowired katanoxDlExchange: DirectExchange,
    ): Binding = BindingBuilder
        .bind(katanoxDlQueue)
        .to(katanoxDlExchange)
        .with(dlRoutingKey)
        .also(admin::declareBinding)

    @Bean
    fun katanoxQueue(): Queue = QueueBuilder
        .durable(queueName)
        .deadLetterExchange(dlExchange)
        .deadLetterRoutingKey(dlRoutingKey)
        .build()
        .also(admin::declareQueue)

    @Bean
    fun katanoxExchange(): DirectExchange = ExchangeBuilder
        .directExchange(exchange)
        .durable(SET_AS_DURABLE)
        .build<DirectExchange>()
        .also(admin::declareExchange)

    @Bean
    fun katanoxBinding(
        @Autowired katanoxQueue: Queue,
        @Autowired katanoxExchange: DirectExchange,
    ): Binding = BindingBuilder
        .bind(katanoxQueue)
        .to(katanoxExchange)
        .with(routingKey)
        .also(admin::declareBinding)

    /**
     * NOTE:
     *   We do not need to explicitly close the connection as it is managed by the RabbitMQ client.
     *   I.e. when the sender is closed, the connection is also closed.
     *   See: https://projectreactor.io/docs/rabbitmq/snapshot/reference/#_closing_the_sender
     */
    @Bean
    @Primary
    fun reactiveConnectionFactory(
        @Autowired rabbitProperties: RabbitProperties,
    ): Mono<Connection> = Mono.fromCallable {
        ConnectionFactory().apply {
            host = rabbitProperties.host
            port = rabbitProperties.port
            username = rabbitProperties.username
            password = rabbitProperties.password
        }.newConnection(REACTIVE_CONNECTION_NAME)
    }.cache()

    @Primary
    @Bean(destroyMethod = "close")
    fun reactiveSender(
        @Autowired @Qualifier("reactiveConnectionFactory") connection: Mono<Connection>,
    ): Sender = RabbitFlux.createSender(
        SenderOptions()
            .connectionMono(connection)
            .resourceManagementScheduler(Schedulers.boundedElastic())
    )

    @Bean
    fun katanoxOutboundMessageTemplate(
        @Autowired jsonMapper: ObjectMapper,
    ) = OutboundMessageTemplate { message, correlationId ->
        OutboundMessage(
            exchange,
            routingKey,
            BasicProperties.Builder()
                .correlationId(correlationId.toString())
                .timestamp(CURRENT_TIMESTAMP_SUPPLIER.invoke())
                .contentEncoding(UTF_8.name())
                .contentType(APPLICATION_JSON_VALUE)
                .deliveryMode(STORED_ON_DISK_PERSISTENCE)
                .build(),
            jsonMapper.writeValueAsBytes(message)
        )
    }
}