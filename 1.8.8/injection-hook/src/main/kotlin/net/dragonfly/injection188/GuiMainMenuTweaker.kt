package net.dragonfly.injection188

import net.dragonfly.agent.tweaker.*
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiMainMenu
import net.minecraft.client.gui.GuiMultiplayer
import kotlin.random.Random

class GuiMainMenuTweaker : Tweaker("net.minecraft.client.gui.GuiMainMenu") {

    @Remap
    private var splashText: String? = null

    @SubstituteMethod
    fun switchToRealms() {
        Minecraft.getMinecraft().displayGuiScreen(GuiMultiplayer(GuiMainMenu()))
    }

    @CopyMethod
    fun generateSplashText() = "fuck you ${Random.nextInt(1, 101)}x"
}