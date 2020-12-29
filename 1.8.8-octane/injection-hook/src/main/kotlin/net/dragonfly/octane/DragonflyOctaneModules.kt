package net.dragonfly.octane

import net.dragonfly.agent.hook.InjectionHookModules
import net.dragonfly.api.GuiProvider
import net.dragonfly.api.surface.IMinecraft
import net.dragonfly.octane.surface.GuiProviderImpl
import net.minecraft.client.Minecraft
import org.koin.dsl.module

object DragonflyOctaneModules : InjectionHookModules {
    override fun modules() = listOf(
        module {
            single<GuiProvider> { GuiProviderImpl }
            single { Minecraft.getMinecraft() as IMinecraft }
        }
    )
}