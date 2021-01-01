package net.dragonfly.core.api

interface IMinecraft {
    val currentScreen: IGuiScreen

    fun displayGuiScreen(screen: IGuiScreen)

    fun runInMinecraftThread(runnable: Runnable)
}