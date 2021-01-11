package net.dragonfly.octane.api.impl

import net.dragonfly.core.api.IGuiMultiplayer
import net.minecraft.client.gui.GuiMultiplayer

class GuiMultiplayerWrapper(wrapped: GuiMultiplayer) : GuiScreenWrapper(wrapped), IGuiMultiplayer {
    override fun connectToSelected() {
        (wrapped as GuiMultiplayer).connectToSelected()
    }
}

fun IGuiMultiplayer.unwrap() = (this as GuiMultiplayerWrapper).wrapped
fun GuiMultiplayer.wrap() = GuiMultiplayerWrapper(this)
