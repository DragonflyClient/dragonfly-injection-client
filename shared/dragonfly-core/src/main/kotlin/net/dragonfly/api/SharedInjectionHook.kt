package net.dragonfly.api

import net.dragonfly.agent.dsl.InstrumentationWrapper
import net.dragonfly.agent.hook.*

object SharedInjectionHook : InjectionHook() {
    override val name: String = "Shared Injection Hook"

    override fun InstrumentationWrapper.configure() {
        tweaker(ClientBrandRetrieverTweaker)
        tweaker(GuiMainMenuTweaker())
    }
}