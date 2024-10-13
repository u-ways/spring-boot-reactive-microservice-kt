package com.katanox.api.config

import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import java.util.TimeZone

/**
 * Time zone configuration class.
 *
 * I have set the time zone using the `spring.jackson.time-zone` property in the
 * `application.properties` file. This way, the time zone can be easily changed without modifying
 * the code whilst ensuring consistency across the application.
 *
 * See: [Spring Boot - JSON Properties](https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html#application-properties.json.spring.jackson.time-zone)
 */
@Configuration
class TimeConfig(
    @Value("\${spring.jackson.time-zone}") private val timeZone: String,
) {
    @PostConstruct
    fun init() = TimeZone.setDefault(TimeZone.getTimeZone(timeZone))
}
