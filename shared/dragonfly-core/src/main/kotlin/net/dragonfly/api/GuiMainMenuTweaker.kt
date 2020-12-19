package net.dragonfly.api

import net.dragonfly.agent.DragonflyAgent
import net.dragonfly.agent.api.*
import net.dragonfly.agent.tweaker.*

class GuiMainMenuTweaker : Tweaker(GuiAPI.switcher!!.mainMenuName) {

    @SubstituteMethod
    fun switchToRealms() {
        val menu = GuiScreenProvider.createDragonflyGuiMainMenu!!.invoke()
        IMinecraft.getInstance().displayGuiScreen(menu)
        DragonflyAgent.getInstance().log("Current screen: " + IMinecraft.getInstance().getCurrentScreen())
    }
}