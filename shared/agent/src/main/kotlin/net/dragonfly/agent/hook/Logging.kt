package net.dragonfly.agent.hook

import net.dragonfly.agent.DragonflyAgent
import org.koin.core.logger.Logger
import org.koin.core.logger.MESSAGE

typealias KoinLevel = org.koin.core.logger.Level

/**
 * Interface that is used by the Dragonfly Agent to publish log messages.
 *
 * The default implementation is [DefaultLoggingProvider]. To implement a custom logging behavior
 * (like for log4j) create a new implementation of this interface and update the [DragonflyAgent.loggingProvider]
 * property.
 */
abstract class LoggingProvider : Logger() {
    abstract fun sendLog(message: String, level: Level, logger: String = "dragonfly-injector")

    override fun log(level: KoinLevel, msg: MESSAGE) {
        val dragonflyLevel = when(level) {
            KoinLevel.DEBUG -> Level.DEBUG
            KoinLevel.INFO -> Level.INFO
            KoinLevel.ERROR -> Level.ERROR
            KoinLevel.NONE -> Level.TRACE
        }
        sendLog(msg, level = dragonflyLevel, logger = "dependency-injection")
    }
}

/**
 * The levels that a log message can have. This may have an influence on how the log message appears in
 * the game output or whether it is displayed.
 */
enum class Level {
    FATAL, ERROR, WARN, INFO, DEBUG, TRACE
}

/**
 * The default implementation of [LoggingProvider] which just appends a the level as a prefix
 * to the message and prints it the standard output stream.
 */
object DefaultLoggingProvider : LoggingProvider() {
    override fun sendLog(message: String, level: Level, logger: String) {
        println("[$logger] [${level.name}]: $message")
    }
}