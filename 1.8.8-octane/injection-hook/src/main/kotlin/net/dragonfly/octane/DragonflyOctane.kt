package net.dragonfly.octane

import net.dragonfly.agent.DragonflyAgent
import net.dragonfly.agent.dsl.InstrumentationWrapper
import net.dragonfly.agent.hook.*
import net.dragonfly.octane.logging.OctaneLoggingProvider

object DragonflyOctane : InjectionHook() {
    override val name: String = "Dragonfly Octane Edition for Minecraft 1.8.8"
    override val simpleName: String = "dragonfly-octane"

    override fun premain(agent: DragonflyAgent) {
        OctaneLoggingProvider.inject(agent)
    }

    override fun InstrumentationWrapper.configure() {
    }
}