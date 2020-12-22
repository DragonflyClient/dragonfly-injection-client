package net.dragonfly.vortex

import net.dragonfly.agent.DragonflyAgent
import net.dragonfly.agent.dsl.InstrumentationWrapper
import net.dragonfly.agent.hook.*
import net.dragonfly.vortex.logging.VortexLoggingProvider

object DragonflyVortex : InjectionHook() {
    override val name: String = "Dragonfly Vortex Edition for Minecraft 1.16.4"

    override fun premain(agent: DragonflyAgent) {
        VortexLoggingProvider.inject(agent)
    }

    override fun InstrumentationWrapper.configure() {
    }
}