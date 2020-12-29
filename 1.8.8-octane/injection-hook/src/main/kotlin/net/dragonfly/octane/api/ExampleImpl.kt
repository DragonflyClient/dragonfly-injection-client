package net.dragonfly.octane.api

import net.dragonfly.core.api.*
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiMultiplayer
import net.minecraft.client.gui.GuiScreen

class MinecraftWrapper : IMinecraft {
    val minecraft = Minecraft.getMinecraft()

    override fun displayGuiScreen(screen: IGuiScreen) {
        minecraft.displayGuiScreen((screen as GuiScreenWrapper).gui)
    }

    override fun getCurrentScreen(): IGuiScreen {
        return when(val currentScreen = minecraft.currentScreen) {
            is GuiMultiplayer -> GuiMultiplayerWrapper(currentScreen)
            else -> GuiScreenWrapper(currentScreen)
        }
    }

    override fun runInMinecraftThread(runnable: Runnable) {
        Minecraft.getMinecraft().addScheduledTask(runnable)
    }
}

open class GuiScreenWrapper(val gui: GuiScreen) : IGuiScreen {
    override fun initGui() {
        gui.initGui()
    }
}

class GuiMultiplayerWrapper(private val guiMultiplayer: GuiMultiplayer) : GuiScreenWrapper(guiMultiplayer), IGuiMultiplayer {
    override fun connectToSelected() {
        guiMultiplayer.connectToSelected()
    }
}