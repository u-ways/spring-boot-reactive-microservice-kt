package com.katanox.api.domain.booking

import com.katanox.api.Application.Companion.LOGGER
import com.katanox.api.config.TraceabilityConfig.Companion.CORRELATION_CONTEXT_KEY
import com.katanox.api.domain.availability.AvailabilityRequest
import com.katanox.api.domain.availability.AvailabilityService
import com.katanox.api.domain.problem.Problem.BadGateway
import com.katanox.api.domain.problem.Problem.InvalidInput
import com.katanox.api.ext.OutboundMessageTemplate
import com.katanox.api.ext.checkFor
import com.katanox.api.ext.logOnNext
import java.util.UUID
import org.slf4j.event.Level
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.rabbitmq.OutboundMessage
import reactor.rabbitmq.SendOptions
import reactor.rabbitmq.Sender

/**
 * # Overview
 *
 * Service that handles the booking of a room.
 *
 * - It checks if the room is available for the given period. If not, it returns an error.
 * - It sends a message to the booking service to reserve the room.
 * - It returns a confirmation message to the client with the booking reference.
 *
 * Which is a simplified version of [Katanox's Booking API](https://api.katanox.com/v2/bookings)
 * You can see the real API documentation [here](https://docs.katanox.com/reference/create-booking).
 *
 * # Implementation Details
 *
 * - Following up on the availability service, the user can use the received availability quote to book a room.
 * - The user needs to provide the hotel details, the room details, the check-in and check-out dates, and their payment details.
 * - The service will check if the room is still available and if the price matches the availability quote.
 * - If everything is correct, it will send a message to the booking service to reserve the room.
 * - To simulate the booking service, we will send a message to a RabbitMQ queue to a "Katanox" exchange.
 *
 * @see AvailabilityService for more details on the availability service functionality.
 */
@Service
class BookingService(
    @Autowired private val availabilityService: AvailabilityService,
    @Autowired private val katanoxOutboundMessageTemplate: OutboundMessageTemplate,
    @Autowired private val reactiveSender: Sender,
) {
    fun reserve(requestMono: Mono<BookingRequest>): Mono<BookingResponse> = requestMono
        .logOnNext(Level.DEBUG)
        .transformDeferredContextual { mono, ctx ->
            val correlationId = ctx
                .get<String>(CORRELATION_CONTEXT_KEY)
                .let(UUID::fromString)

            mono.flatMap { bookingRequest ->
                availabilityService
                    .search(Mono.just(AvailabilityRequest(bookingRequest.hotelId, bookingRequest.checkIn, bookingRequest.checkOut)))
                    .logOnNext(Level.DEBUG)
                    .collectList()
                    .checkFor(InvalidInput("Currency does not match hotel requirements.")) {
                        any { it.roomId == bookingRequest.roomId && it.currency == bookingRequest.currency }
                    }
                    .checkFor(InvalidInput("Price does not match the availability quote for room: ${bookingRequest.roomId}")) {
                        any { it.roomId == bookingRequest.roomId && it.price == bookingRequest.price }
                    }
                    .flatMap {
                        reactiveSender
                            .sendWithPublishConfirms(
                                katanoxOutboundMessageTemplate
                                    .prepare(BookingDTO(correlationId, bookingRequest), correlationId)
                                    .let { outboundMessage -> Mono.just(outboundMessage) }
                                    .logOnNext(Level.DEBUG),
                                SendOptions()
                                    .trackReturned(ENSURE_MESSAGES_ROUTED_TO_QUEUE)
                                    .maxInFlight(CONFIRMS_BACKPRESSURE_BUFFER_SIZE)
                                    .exceptionHandler { context: Sender.SendContext<OutboundMessage>, exception: Exception ->
                                        LOGGER.error("Error sending message: {}", context.message, exception)
                                    }
                            )
                            .single()
                            .logOnNext(Level.DEBUG)
                            .checkFor(BadGateway("Message was not acknowledged by the broker.")) { isAck }
                            .checkFor(BadGateway("Message was returned by the broker.")) { isReturned.not() }
                            .map { BookingResponse(correlationId) }
                    }
            }
        }

    companion object {
        // NOTE: In production, this should be a runtime configuration value.
        private const val CONFIRMS_BACKPRESSURE_BUFFER_SIZE = 250
        private const val ENSURE_MESSAGES_ROUTED_TO_QUEUE = true
    }
}