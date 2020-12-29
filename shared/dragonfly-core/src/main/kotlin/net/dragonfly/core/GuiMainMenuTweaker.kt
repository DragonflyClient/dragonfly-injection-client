package net.dragonfly.core

import net.dragonfly.agent.tweaker.*
import net.dragonfly.core.api.IGuiMultiplayer
import net.dragonfly.core.api.IMinecraft
import kotlin.concurrent.thread

class GuiMainMenuTweaker : Tweaker() {

    @Substitute
    fun switchToRealms() {
        val mc = IMinecraft.getInstance()
        mc.displayGuiScreen(IGuiMultiplayer.create(mc.getCurrentScreen()))
        thread(start = true) {
            Thread.sleep(5000)
            mc.runInMinecraftThread {
                (mc.getCurrentScreen() as IGuiMultiplayer).connectToSelected()
            }
        }
    }
}