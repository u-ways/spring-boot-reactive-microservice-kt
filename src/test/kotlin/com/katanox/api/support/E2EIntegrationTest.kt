package com.katanox.api.support

import com.katanox.api.sql.tables.references.HOTELS
import com.katanox.api.support.domain.builder.HotelsBuilder
import com.katanox.api.support.domain.factory.DomainDaoFactory
import com.katanox.api.support.domain.matcher.KatanoxQueueMatchers
import io.restassured.RestAssured
import io.restassured.response.Response
import io.restassured.response.ValidatableResponse
import io.restassured.specification.RequestSender
import io.restassured.specification.RequestSpecification
import org.jooq.DSLContext
import org.junit.jupiter.api.BeforeEach
import org.springframework.amqp.core.Message
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.web.server.LocalServerPort
import java.time.LocalDate
import java.util.Currency
import java.util.UUID
import java.util.function.Supplier

/**
 * Base class for end-to-end integration tests, providing a DSL for constructing and executing test scenarios.
 *
 * This class extends the [IntegrationTest] class, providing the necessary setup for running integration tests
 * within the Spring context. It also provides a DSL for constructing test scenarios, following the BDD pattern.
 *
 * @see [IntegrationTest] for details on the base class for integration tests.
 */
@Suppress("TestFunctionName")
@ImportAutoConfiguration(BlockingJooqConfig::class, BlockingAmqpConfig::class)
abstract class E2EIntegrationTest : IntegrationTest {
    companion object {
        internal val GIVEN_CORRELATION_ID = UUID.randomUUID()

        internal const val HOTEL_A = 1L
        internal val HOTEL_A_VAT = 0.1.toBigDecimal()
        internal val HOTEL_A_CURRENCY = Currency.getInstance("EUR")
        internal val HOTEL_A_FLAT_PRICE_ONCE = 25.toBigDecimal()
        internal val HOTEL_A_FLAT_PRICE_PER_NIGHT = 5.toBigDecimal()
        internal val HOTEL_A_FLAT_PRICE_PERCENT = 10.toBigDecimal()

        internal const val ROOM_A1 = 1L
        internal val ROOM_A1_FIRST_PRICE = 103.toBigDecimal()
        internal val ROOM_A1_FIRST_DATE = LocalDate.parse("2022-04-01")
        internal val ROOM_A1_SECOND_PRICE = 99.toBigDecimal()
        internal val ROOM_A1_SECOND_DATE = LocalDate.parse("2022-04-02")
        internal val ROOM_A1_THIRD_PRICE = 110.toBigDecimal()
        internal val ROOM_A1_THIRD_DATE = LocalDate.parse("2022-04-03")

        internal const val ROOM_A2 = 2L
        internal val ROOM_A2_FIRST_PRICE = 113.toBigDecimal()
        internal val ROOM_A2_FIRST_DATE = LocalDate.parse("2022-04-01")
        internal val ROOM_A2_SECOND_PRICE = 109.toBigDecimal()
        internal val ROOM_A2_SECOND_DATE = LocalDate.parse("2022-04-02")
        internal val ROOM_A2_THIRD_PRICE = 123.toBigDecimal()
        internal val ROOM_A2_THIRD_DATE = LocalDate.parse("2022-04-03")

        internal const val EXPECTED_TOTAL_FOR_ROOM_A1 = 267.5
        internal const val EXPECTED_TOTAL_FOR_ROOM_A2 = 290.5

        internal const val HOTEL_B = 2L
        internal const val ROOM_B1 = 3L
        internal val HOTEL_B_VAT = 0.2.toBigDecimal()
        internal val HOTEL_B_CURRENCY = Currency.getInstance("USD")

        internal const val GUEST_NAME = "John"
        internal const val GUEST_SURNAME = "Doe"
        internal const val GUEST_BOOKING_PRICE = 100.0
        internal val GUEST_CURRENCY = Currency.getInstance("EUR")
        internal val GUEST_BIRTHDATE = LocalDate.parse("1990-01-01")

        internal const val PAYMENT_CARD_HOLDER = "John Doe"
        internal const val PAYMENT_CARD_NUMBER = "1234567890123456"
        internal const val PAYMENT_CVV = "123"
        internal const val PAYMENT_EXPIRY_MONTH = "01"
        internal const val PAYMENT_EXPIRY_YEAR = "2023"
    }

    @LocalServerPort
    private val port: Int = 0

    @Autowired
    private lateinit var domainDaoFactory: DomainDaoFactory

    @Autowired
    lateinit var katanoxQueueReceiver: Supplier<Message?>

    @Autowired
    lateinit var katanoxDlQueueReceiver: Supplier<Message?>

    @Autowired
    @Qualifier("blockingJooq")
    private lateinit var jooq: DSLContext

    @BeforeEach
    fun setUp() {
        RestAssured.port = port

        with(jooq) {
            deleteFrom(HOTELS).execute()
            assert(fetch(HOTELS).isEmpty()) { "Database was not cleaned up after test" }
        }
    }

    /**
     * This is a simple DSL to make RestAssured more readable within our acceptance tests.
     *
     * The [Specification] relies on the [RequestSpecification] interface to delegate the request to
     * RestAssured, while adding a [RequestSender] to the request. Allowing you to use the `When` infix
     * function to describe the entire request.
     *
     * see: [Kotlin - Concepts - Delegation](https://kotlinlang.org/docs/delegation.html)
     */
    protected data class Specification(
        val requestSender: RequestSender,
    ) : RequestSpecification by RestAssured.given()

    /**
     * Primes the database with the given domain data, intended to be used at the beginning of a test scenario.
     * This function sets the initial state for a scenario, following the 'Given' step in BDD, where you describe
     * the initial context of the system.
     *
     * @param block the block to be applied to the request, allowing for detailed specification of the initial state.
     * @see [HotelsBuilder] for details on constructing hotel domain objects within this context.
     */
    protected fun Given(block: HotelsBuilder.() -> Unit) =
        HotelsBuilder.hotel()
            .apply(block)
            .build()
            .let(domainDaoFactory::create)

    /**
     * Primes the database with additional domain data, to be used after a 'Given' step in a test scenario.
     *
     * The 'And' function serves as a continuation of the setup process, allowing for the extension of the initial context
     * or the addition of more complex scenarios.
     *
     * @param block the block to be applied to the request, facilitating the extension or addition of test conditions.
     * @see [HotelsBuilder] for constructing additional hotel domain objects in this extended context.
     */
    protected fun And(block: HotelsBuilder.() -> Unit) =
        HotelsBuilder.hotel()
            .apply(block)
            .build()
            .let(domainDaoFactory::create)

    /**
     * Executes the main action of the test scenario, marking the transition from setting the context to performing an
     * action that triggers behavior. This function encapsulates the action phase of a BDD scenario.
     *
     * @param block the block to be applied to the action execution, defining the action and generating the response.
     * @return the [Response] from the system after the action has been executed, for further validation.
     * @see [Specification] for details on the request specification and execution.
     * @see [Response.Then] for details on validating the response.
     */
    protected fun When(block: RequestSpecification.() -> Unit): RequestSpecification = RestAssured.given().apply(block)

    protected infix fun RequestSpecification.Executes(block: RequestSender.() -> Response): Response = this.`when`().run(block)

    /**
     * Validates the outcome of the test scenario, following the action performed in the [When] step. This function
     * applies assertions to the system's response, checking that the system behaves as expected after the action,
     * aligning with BDD's outcome validation phase.
     *
     * @param block the function to be applied to the response for validation, containing assertions or checks.
     * @return the [ValidatableResponse], allowing for further chained validations if necessary.
     */
    protected infix fun Response.Then(block: ValidatableResponse.() -> Unit): ValidatableResponse = this.then().apply(block)

    /**
     * Validates the AMQP outcome of the test scenario, following the action performed in the [Then] step. This function
     * applies assertions to the system's queue, checking that the application publishers behave as expected after the action.
     *
     * @param block the function to be applied to the message queues for validation, containing assertions or checks.
     * @See [KatanoxQueueMatchers] for details on the AMQP message queue matcher.
     */
    protected infix fun ValidatableResponse.And(block: KatanoxQueueMatchers.() -> Unit) =
        block(KatanoxQueueMatchers(katanoxQueueReceiver, katanoxDlQueueReceiver))
}
