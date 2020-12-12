package net.dragonfly.injection.shared

import net.dragonfly.agent.api.GuiAPI
import net.dragonfly.agent.tweaker.*

class GuiMainMenuTweaker : Tweaker(GuiAPI.switcher!!.mainMenuName) {

    @Remap
    var splashText: String? = null

    @SubstituteMethod
    fun switchToRealms() {
//        splashText = "Welcome to Minecraft " + GuiAPI.switcher?.version
        GuiAPI.switcher?.switchToMultiplayer()
    }
}