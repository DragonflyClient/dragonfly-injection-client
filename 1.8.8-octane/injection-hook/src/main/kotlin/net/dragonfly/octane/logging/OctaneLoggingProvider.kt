package net.dragonfly.octane.logging

import net.dragonfly.agent.DragonflyAgent
import net.dragonfly.agent.hook.Level
import net.dragonfly.agent.hook.LoggingProvider
import org.apache.logging.log4j.LogManager

typealias Log4jLevel = org.apache.logging.log4j.Level

object OctaneLoggingProvider : LoggingProvider() {
    override fun sendLog(message: String, level: Level) {
        LogManager.getLogger("dragonfly-injector").log(translateLevel(level), message)
    }

    private fun translateLevel(input: Level): Log4jLevel = when(input) {
        Level.FATAL -> Log4jLevel.FATAL
        Level.ERROR -> Log4jLevel.ERROR
        Level.WARN -> Log4jLevel.WARN
        Level.INFO -> Log4jLevel.INFO
        Level.DEBUG -> Log4jLevel.DEBUG
        Level.TRACE -> Log4jLevel.TRACE
    }

    fun inject(agent: DragonflyAgent) {
        agent.loggingProvider = this
    }
}