package net.dragonfly.core

import net.dragonfly.agent.hook.get
import net.dragonfly.agent.tweaker.*
import net.dragonfly.core.api.IGuiMultiplayer
import net.dragonfly.core.api.IMinecraft
import org.koin.core.parameter.parametersOf
import kotlin.concurrent.thread

class GuiMainMenuTweaker : Tweaker() {

    @Substitute
    fun switchToRealms() {
        val mc: IMinecraft = get()
        val multiplayerGui: IGuiMultiplayer = get { parametersOf(mc.currentScreen) }
        mc.displayGuiScreen(multiplayerGui)

        thread(start = true) {
            Thread.sleep(5000)
            mc.runInMinecraftThread {
                (mc.currentScreen as IGuiMultiplayer).connectToSelected()
            }
        }
    }
}