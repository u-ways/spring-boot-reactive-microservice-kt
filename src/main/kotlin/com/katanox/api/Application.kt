package com.katanox.api

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class Application {
    companion object {
        internal val LOGGER: Logger = LoggerFactory
            .getLogger(Application::class.java.`package`.name)

        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<Application>(*args)
        }
    }
}
