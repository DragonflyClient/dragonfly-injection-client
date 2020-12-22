package net.dragonfly.vision

import net.dragonfly.agent.DragonflyAgent
import net.dragonfly.agent.dsl.InstrumentationWrapper
import net.dragonfly.agent.hook.*
import net.dragonfly.vision.logging.VisionLoggingProvider

object DragonflyVision : InjectionHook() {
    override val name: String = "Dragonfly Vision Edition Minecraft 1.12"

    override fun premain(agent: DragonflyAgent) {
        VisionLoggingProvider.inject(agent)
    }

    override fun InstrumentationWrapper.configure() {
    }
}