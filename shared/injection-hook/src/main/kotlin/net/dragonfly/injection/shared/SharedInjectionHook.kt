package net.dragonfly.injection.shared

import net.dragonfly.agent.DragonflyAgent
import net.dragonfly.agent.dsl.InstrumentationWrapper
import net.dragonfly.agent.dsl.editMethod
import net.dragonfly.agent.hook.*

object SharedInjectionHook : InjectionHook() {
    override val name: String = "Shared Injection Hook"

    override fun premain(agent: DragonflyAgent) {
        agent.loggingProvider = object : LoggingProvider {
            override fun sendLog(message: String, level: Level) {
                println(message)
            }
        }
    }

    override fun InstrumentationWrapper.configure() {
        editClass("net.minecraft.client.ClientBrandRetriever") {
            editMethod("getClientModName") {
                setBody("""
                    {
                        return "Core Injection Hook";
                    }
                """.trimIndent())
            }
        }
    }
}