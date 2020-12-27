@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.dragonfly.core

import net.dragonfly.agent.dsl.InstrumentationWrapper
import net.dragonfly.agent.hook.*
import net.dragonfly.api.GuiProvider

object SharedInjectionHook : InjectionHook() {
    override val name: String = "Shared Injection Hook"

    private val guiProvider: GuiProvider by inject()

    override fun InstrumentationWrapper.configure() {
//        tweaker(ClientBrandRetrieverTweaker)
        tweaker(GuiMainMenuTweaker())
    }
}