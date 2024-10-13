package com.katanox.api.support

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.ContextConfiguration

/**
 * This interface is used for setting up the Spring context for integration tests.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ContextConfiguration(classes = [InfrastructureConfiguration::class])
interface IntegrationTest
