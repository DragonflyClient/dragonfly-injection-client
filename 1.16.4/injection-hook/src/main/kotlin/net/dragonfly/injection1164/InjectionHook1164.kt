package net.dragonfly.injection1164

import net.dragonfly.agent.DragonflyAgent
import net.dragonfly.agent.api.GuiAPI
import net.dragonfly.agent.api.GuiSwitcher
import net.dragonfly.agent.dsl.InstrumentationWrapper
import net.dragonfly.agent.hook.*
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screen.MainMenuScreen
import net.minecraft.client.gui.screen.MultiplayerScreen
import org.apache.logging.log4j.LogManager
import kotlin.concurrent.thread

object InjectionHook1164 : InjectionHook() {
    override val name: String = "Core Injection Hook Minecraft 1.16.4"

    override fun premain(agent: DragonflyAgent) {
        GuiAPI.switcher = object : GuiSwitcher {
            override val version: String
                get() = Minecraft.getInstance().version
            override val mainMenuName: String = "net.minecraft.client.gui.screen.MainMenuScreen"

            override fun switchToMultiplayer() {
                thread(start = true) {
                    Thread.sleep(1_000)
                    val minecraft = Minecraft.getInstance()
                    val runnable = object : Runnable {
                        override fun run() {
                            Minecraft.getInstance().displayGuiScreen(MultiplayerScreen(MainMenuScreen()))
                        }
                    } as Runnable
                    minecraft.runImmediately(runnable)
                }
            }
        }
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
    }
}