package net.dragonfly.octane.api.impl

import net.dragonfly.core.api.IGuiScreen
import net.dragonfly.core.api.IMinecraft
import net.minecraft.client.Minecraft

class MinecraftWrapper(val wrapped: Minecraft) : IMinecraft {
    override val currentScreen: IGuiScreen
        get() = wrapped.currentScreen.wrap()

    override fun displayGuiScreen(screen: IGuiScreen) =
        wrapped.displayGuiScreen(screen.unwrap())

    override fun runInMinecraftThread(runnable: Runnable) {
        wrapped.addScheduledTask(runnable)
    }
}

fun IMinecraft.unwrap() = (this as MinecraftWrapper).wrapped
fun Minecraft.wrap() = MinecraftWrapper(this)
