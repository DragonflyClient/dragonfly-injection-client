package net.dragonfly.injection.shared

import net.dragonfly.agent.api.GuiAPI
import net.dragonfly.agent.tweaker.*

class GuiMainMenuTweaker : Tweaker("net.minecraft.client.gui.GuiMainMenu") {

    @SubstituteMethod
    fun switchToRealms() {
        GuiAPI.switcher?.switchToMultiplayer()
    }
}