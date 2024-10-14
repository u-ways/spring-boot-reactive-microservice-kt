package com.katanox.api.config

import org.flywaydb.core.Flyway
import org.springframework.boot.autoconfigure.flyway.FlywayProperties
import org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Usually, there is no need to create this class manually, as Spring Boot will
 * automatically configure the migration based on the properties in the `application.properties`
 *
 * However, Flyway doesn't yet support the R2DBC driver, so we need to do some manual configuration
 * to make it work via the JDBC driver.
 *
 * See: [GitHub - Flyway #2502](https://github.com/flyway/flyway/issues/2502)
 */
@Configuration
@EnableConfigurationProperties(FlywayProperties::class, R2dbcProperties::class)
class FlywayConfig {
    @Bean(initMethod = "migrate")
    fun flyway(
        flywayProperties: FlywayProperties,
        r2dbcProperties: R2dbcProperties,
    ): Flyway =
        Flyway
            .configure()
            .dataSource(flywayProperties.url, r2dbcProperties.username, r2dbcProperties.password)
            .locations(*flywayProperties.locations.toTypedArray())
            .baselineOnMigrate(flywayProperties.isBaselineOnMigrate)
            .schemas(*flywayProperties.schemas.toTypedArray())
            .load()
}
