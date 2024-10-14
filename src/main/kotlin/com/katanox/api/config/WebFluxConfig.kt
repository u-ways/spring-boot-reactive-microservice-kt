package com.katanox.api.config

import com.katanox.api.domain.availability.AvailabilityHandler
import com.katanox.api.domain.booking.BookingHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router

/**
 * Web configuration class.
 *
 * WebFlux is a non-blocking reactive framework that is part of Spring 5.
 *
 * See: [WebFlux - configuration](https://docs.spring.io/spring-framework/reference/web/webflux/config.html)
 */
@EnableWebFlux
@Configuration
class WebFluxConfig : WebFluxConfigurer {
    /**
     * Spring WebFlux includes `WebFlux.fn`, a lightweight functional programming model in which functions are
     * used to route and handle requests and contracts are designed for immutability. It is an alternative to
     * the annotation-based programming model but otherwise runs on the same Reactive Core foundation.
     *
     * See: [WebFlux - Functional Endpoints](https://docs.spring.io/spring-framework/reference/web/webflux-functional.html)
     */
    @Bean
    fun routes(
        @Autowired availabilityHandler: AvailabilityHandler,
        @Autowired bookingHandler: BookingHandler,
    ): RouterFunction<ServerResponse> =
        router {
            (contentType(MediaType.APPLICATION_JSON) and accept(MediaType.APPLICATION_JSON)).nest {
                GET(AvailabilityHandler.PATH, availabilityHandler::handle)
                POST(BookingHandler.PATH, bookingHandler::handle)
            }
        }
}
