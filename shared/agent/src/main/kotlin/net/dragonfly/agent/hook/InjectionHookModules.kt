package net.dragonfly.agent.hook

import org.koin.core.module.Module

/**
 * Interface to be implemented by additional parts of the injection hook to provide
 * dependency injection modules.
 *
 * The Kotlin objects must have the name of the injection hook class followed by
 * "Modules", e.g: `DragonflyOctane` → `DragonflyOctaneModules`.
 */
interface InjectionHookModules {

    /**
     * To return a list of dependency injection modules.
     */
    fun modules(): List<Module> = emptyList()
}