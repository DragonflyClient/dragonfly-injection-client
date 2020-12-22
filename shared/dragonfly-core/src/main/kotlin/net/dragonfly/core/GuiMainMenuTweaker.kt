package net.dragonfly.core

import net.dragonfly.agent.tweaker.*

class GuiMainMenuTweaker(guiName: String) : Tweaker(guiName) {

    @Inject
    var timesPressed: Int = 0

    @Redirect
    var splashText: String? = null

    @Redirect(origin = Origin(DynamicOrigin.SUPERCLASS))
    var buttonList: MutableList<Any> = mutableListOf()

    @Substitute
    fun switchToRealms() {
        splashText = "Button pressed ${++timesPressed} times"

        if (timesPressed % 2 != 0) {
            addSingleplayerMultiplayerButtons(10, 10)
        } else {
            buttonList.removeLast()
            buttonList.removeLast()
            buttonList.removeLast()
        }
    }

    @Redirect
    fun addSingleplayerMultiplayerButtons(a: Int, b: Int) {}
}