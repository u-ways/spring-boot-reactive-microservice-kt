package com.katanox.api.support.logging

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase

/**
 * An Appender that stores all the log events in memory.
 * This is useful for testing purposes.
 */
class InMemoryAppender(
    private val requiredLevel: Level
) : AppenderBase<ILoggingEvent>() {
    private val events: MutableList<String> = ArrayList()

    internal fun events(): List<String> = events.toList()

    override fun append(eventObject: ILoggingEvent) {
        if (eventObject.level.isGreaterOrEqual(requiredLevel))
            events.add(eventObject.message)
    }
}
