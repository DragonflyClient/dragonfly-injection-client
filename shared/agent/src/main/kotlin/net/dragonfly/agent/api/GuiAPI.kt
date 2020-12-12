package net.dragonfly.agent.api

object GuiAPI {
    var switcher: GuiSwitcher? = null
}

interface GuiSwitcher {
    val version: String?
    val mainMenuName: String
    fun switchToMultiplayer()
}