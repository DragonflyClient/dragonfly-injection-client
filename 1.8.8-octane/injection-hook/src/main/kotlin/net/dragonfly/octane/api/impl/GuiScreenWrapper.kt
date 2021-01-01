package net.dragonfly.octane.api.impl

import net.dragonfly.core.api.IGuiScreen
import net.minecraft.client.gui.GuiMultiplayer
import net.minecraft.client.gui.GuiScreen

open class GuiScreenWrapper(val wrapped: GuiScreen) : IGuiScreen

fun IGuiScreen.unwrap() = (this as GuiScreenWrapper).wrapped
fun GuiScreen.wrap() = when(this) {
    is GuiMultiplayer -> wrap()
    else -> GuiScreenWrapper(this)
}
