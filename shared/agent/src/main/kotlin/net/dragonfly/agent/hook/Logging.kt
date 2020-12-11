package net.dragonfly.agent.hook

import net.dragonfly.agent.DragonflyAgent

/**
 * Interface that is used by the Dragonfly Agent to publish log messages.
 *
 * The default implementation is [DefaultLoggingProvider]. To implement a custom logging behavior
 * (like for log4j) create a new implementation of this interface and update the [DragonflyAgent.loggingProvider]
 * property.
 */
interface LoggingProvider {
    fun sendLog(message: String, level: Level)
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
object DefaultLoggingProvider : LoggingProvider {
    override fun sendLog(message: String, level: Level) {
        println("[${level.name}]: $message")
    }
}