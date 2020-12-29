package net.dragonfly.octane

import net.dragonfly.agent.hook.InjectionHookModules
import net.dragonfly.core.api.*
import net.dragonfly.octane.api.*
import net.minecraft.client.gui.GuiMultiplayer
import org.koin.dsl.module

object DragonflyOctaneModules : InjectionHookModules {
    override fun modules() = listOf(
        module {
            single<IMinecraft> { MinecraftWrapper() }
            factory<IGuiMultiplayer> { (parentScreen: IGuiScreen) ->
                GuiMultiplayerWrapper(GuiMultiplayer((parentScreen as GuiScreenWrapper).gui))
            }
        }
    )
}