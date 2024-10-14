package com.katanox.api.support.logging

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger

/**
 * A utility class that collects all the logs of a given logger.
 * This is useful for testing purposes.
 */
class LogsCollector(
    private val logger: Logger,
    private val requiredLevel: Level = Level.INFO,
) {
    private val originalLevel = logger.level
    private lateinit var inMemoryAppender: InMemoryAppender

    internal fun start() {
        inMemoryAppender =
            InMemoryAppender(requiredLevel)
                .also(logger::addAppender)
                .also(InMemoryAppender::start)
                .apply { logger.level = requiredLevel }
    }

    internal fun stop() {
        inMemoryAppender.stop()
        logger.detachAppender(inMemoryAppender)
        logger.level = originalLevel
    }

    fun logs(): List<String> = inMemoryAppender.events()

    fun find(predicate: (String) -> Boolean): String? = logs().find(predicate)
}
