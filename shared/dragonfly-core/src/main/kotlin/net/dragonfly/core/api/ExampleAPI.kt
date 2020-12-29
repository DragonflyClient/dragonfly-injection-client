package net.dragonfly.core.api

import net.dragonfly.agent.hook.get
import org.koin.core.parameter.parametersOf

interface IMinecraft {
    fun displayGuiScreen(screen: IGuiScreen)
    fun getCurrentScreen(): IGuiScreen
    fun runInMinecraftThread(runnable: Runnable)

    companion object {
        fun getInstance() = get<IMinecraft>()
    }
}

interface IGuiScreen {
    fun initGui()
}

interface IGuiMultiplayer : IGuiScreen {
    fun connectToSelected()

    companion object {
        fun create(parentScreen: IGuiScreen) = get<IGuiMultiplayer> { parametersOf(parentScreen) }
    }
}