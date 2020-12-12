package net.dragonfly.agent.api

object GuiAPI {
    var switcher: GuiSwitcher? = null
}

interface GuiSwitcher {
    fun switchToMultiplayer()
}