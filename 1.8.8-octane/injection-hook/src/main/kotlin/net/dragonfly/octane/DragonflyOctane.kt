package net.dragonfly.octane

import net.dragonfly.agent.DragonflyAgent
import net.dragonfly.agent.dsl.InstrumentationWrapper
import net.dragonfly.agent.hook.*
import org.apache.logging.log4j.LogManager

typealias Log4jLevel = org.apache.logging.log4j.Level

object InjectionHook188 : InjectionHook() {
    override val name: String = "Core Injection Hook Minecraft 1.8.8"

    override fun premain(agent: DragonflyAgent) {
        agent.loggingProvider = object : LoggingProvider {
            override fun sendLog(message: String, level: Level) {
                LogManager.getLogger("dragonfly-injector").log(translateLevel(level), message)
            }

            private fun translateLevel(input: Level) = when(input) {
                Level.FATAL -> Log4jLevel.FATAL
                Level.ERROR -> Log4jLevel.ERROR
                Level.WARN -> Log4jLevel.WARN
                Level.INFO -> Log4jLevel.INFO
                Level.DEBUG -> Log4jLevel.DEBUG
                Level.TRACE -> Log4jLevel.TRACE
            }
        }
    }

    override fun InstrumentationWrapper.configure() {
    }
}