package net.dragonfly.octane.surface

import net.dragonfly.api.GuiProvider
import net.dragonfly.api.surface.IGuiScreen
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiMultiplayer

object GuiProviderImpl : GuiProvider {
    override fun getMainMenu() = "net.minecraft.client.gui.GuiMainMenu"
    override fun createMultiplayerMenu() = GuiMultiplayer(Minecraft.getMinecraft().currentScreen) as IGuiScreen
}