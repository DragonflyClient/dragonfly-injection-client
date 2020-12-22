package net.dragonfly.octane

import net.dragonfly.agent.DragonflyAgent
import net.dragonfly.agent.dsl.InstrumentationWrapper
import net.dragonfly.agent.hook.*
import net.dragonfly.api.GuiProvider
import net.dragonfly.octane.logging.OctaneLoggingProvider
import org.koin.dsl.module

typealias Log4jLevel = org.apache.logging.log4j.Level

object DragonflyOctane : InjectionHook() {
    override val name: String = "Dragonfly Octane Edition for Minecraft 1.8.8"

    override fun premain(agent: DragonflyAgent) {
        OctaneLoggingProvider.inject(agent)
    }

    override fun InstrumentationWrapper.configure() {
    }

    private val guiProvider = object : GuiProvider {
        override fun getMainMenu() = "net.minecraft.client.gui.GuiMainMenu"
    }

    override fun modules() = listOf(
        module {
            single<GuiProvider> { guiProvider }
        }
    )
}