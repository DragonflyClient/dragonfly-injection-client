@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.dragonfly.core

import net.dragonfly.agent.dsl.InstrumentationWrapper
import net.dragonfly.agent.hook.*

object DragonflyCore : InjectionHook() {
    override val name: String = "Dragonfly Core"
    override val simpleName: String = "dragonfly-core"

    override fun InstrumentationWrapper.configure() {
        tweaker(GuiMainMenuTweaker())
    }
}