package net.dragonfly.octane

import net.dragonfly.agent.hook.InjectionHookModules
import net.dragonfly.core.api.*
import net.dragonfly.octane.api.impl.unwrap
import net.dragonfly.octane.api.impl.wrap
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiMultiplayer
import org.koin.dsl.module

object DragonflyOctaneModules : InjectionHookModules {
    override fun modules() = listOf(
        module {
            single<IMinecraft> { Minecraft.getMinecraft().wrap() }
            factory<IGuiMultiplayer> { (parentScreen: IGuiScreen) -> GuiMultiplayer(parentScreen.unwrap()).wrap() }
        }
    )
}