package net.dragonfly.injection188

import net.dragonfly.agent.DragonflyAgent
import net.dragonfly.agent.dsl.InstrumentationWrapper
import net.dragonfly.agent.hook.*
import org.apache.logging.log4j.LogManager

object CoreInjectionHook : InjectionHook() {
    override val name: String = "Core Injection Hook Minecraft 1.8.8"

    override fun premain(agent: DragonflyAgent) {
        agent.loggingProvider = object : LoggingProvider {
            override fun sendLog(message: String, level: Level) {
                LogManager.getLogger("dragonfly-injector").log(translateLevel(level), message)
            }

            private fun translateLevel(input: Level): org.apache.logging.log4j.Level = when(input) {
                Level.FATAL -> org.apache.logging.log4j.Level.FATAL
                Level.ERROR -> org.apache.logging.log4j.Level.ERROR
                Level.WARN -> org.apache.logging.log4j.Level.WARN
                Level.INFO -> org.apache.logging.log4j.Level.INFO
                Level.DEBUG -> org.apache.logging.log4j.Level.DEBUG
                Level.TRACE -> org.apache.logging.log4j.Level.TRACE
            }
        }
    }

    override fun InstrumentationWrapper.configure() {
        tweaker(GuiMainMenuTweaker())
    }
}