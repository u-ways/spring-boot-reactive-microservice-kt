package com.katanox.api.support

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.springframework.test.context.DynamicPropertyRegistry
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.utility.DockerImageName

/**
 * The Testcontainers library provides a way to manage services running inside Docker containers.
 *
 * It integrates with JUnit, allowing you to write a test class that can start up a container
 * before any of the tests run. Testcontainers is especially useful for writing integration
 * tests that talk to a real backend service such as MySQL, MongoDB, Cassandra and others.
 *
 * Spring has built-in support for Testcontainers, allowing you to use during testing.
 *
 * See: https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#features.testing.testcontainers.dynamic-properties
 */
@TestConfiguration(proxyBeanMethods = false)
class InfrastructureConfiguration {
    /**
     * The `@[ServiceConnection]` annotation allows you to automatically inject the connection details
     * of a container into your test class without having to manually set up the properties via
     * the `[DynamicPropertyRegistry]`.
     *
     * See: https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#features.testing.testcontainers.service-connections
     */
    @Bean
    @ServiceConnection
    fun rabbitContainer(properties: DynamicPropertyRegistry): RabbitMQContainer =
        RabbitMQContainer(DockerImageName.parse("rabbitmq:latest")).apply {
            properties.add("spring.rabbitmq.host", this::getHost)
            properties.add("spring.rabbitmq.port", this::getAmqpPort)
            properties.add("spring.rabbitmq.username", this::getAdminUsername)
            properties.add("spring.rabbitmq.password", this::getAdminPassword)
        }

    /**
     * Usually, you should use the `@[ServiceConnection]` annotation to inject the connection details
     * of the container into your test class. However, in this case, we are using the `[DynamicPropertyRegistry]`
     * to manage complex reactive and non-reactive infrastructure properties that are not supported by the
     * service connections feature.
     *
     * See: https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#features.testing.testcontainers.dynamic-properties
     */
    @Bean
    fun postgresContainer(properties: DynamicPropertyRegistry): PostgreSQLContainer<*> =
        PostgreSQLContainer(DockerImageName.parse("postgres:latest")).apply {
            properties.add("spring.r2dbc.url") { jdbcUrl.replace("jdbc:", "r2dbc:") }
            properties.add("spring.r2dbc.username", this::getUsername)
            properties.add("spring.r2dbc.password", this::getPassword)
            properties.add("spring.flyway.url", this::getJdbcUrl)
            properties.add("spring.flyway.driver-class-name", this::getDriverClassName)
        }
}
