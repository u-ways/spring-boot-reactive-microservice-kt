package com.katanox.api.config

import io.r2dbc.spi.ConnectionFactory
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

/**
 * Sets jOOQ [DSLContext] to a reactive [ConnectionFactory].
 *
 * See: [jOOQ - Reactive Fetching](https://www.jooq.org/doc/latest/manual/sql-execution/fetching/reactive-fetching/)
 */
@Configuration
class JooqConfig {
    @Bean
    @Primary
    fun jooq(connectionFactory: ConnectionFactory): DSLContext = DSL.using(connectionFactory)
}
