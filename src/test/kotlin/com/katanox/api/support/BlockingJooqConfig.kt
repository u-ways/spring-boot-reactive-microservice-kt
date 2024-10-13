package com.katanox.api.support

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.springframework.boot.autoconfigure.flyway.FlywayProperties
import org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import javax.sql.DataSource

/**
 * A transactional blocking instance of the jOOQ DSL.
 *
 * This MUST only be used within the test context. The current usecase
 * is for priming a database setup for our end-to-end tests.
 */
@Configuration
@EnableTransactionManagement
@EnableConfigurationProperties(FlywayProperties::class, R2dbcProperties::class)
class BlockingJooqConfig {
    @Bean(destroyMethod = "close")
    fun dataSource(
        flywayProperties: FlywayProperties,
        r2dbcProperties: R2dbcProperties,
    ) = HikariConfig().apply {
        jdbcUrl = flywayProperties.url
        username = r2dbcProperties.username
        password = r2dbcProperties.password
        driverClassName = flywayProperties.driverClassName
    }.let(::HikariDataSource)

    @Bean
    fun transactionManager(dataSource: DataSource): PlatformTransactionManager = DataSourceTransactionManager(dataSource)

    @Bean
    fun blockingJooq(dataSource: DataSource): DSLContext = DSL.using(dataSource, SQLDialect.POSTGRES)
}
