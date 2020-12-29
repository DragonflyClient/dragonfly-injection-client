package net.dragonfly.agent.hook

import net.dragonfly.agent.DragonflyAgent
import net.dragonfly.agent.dsl.InstrumentationWrapper
import org.koin.core.module.Module

/**
 * An interface that allows so-called injection hooks to add functionality to the default
 * Dragonfly Agent (which does nothing special without them).
 *
 * Every injection hook must have a bootstrap class that implements this class. Its methods
 * are called during different steps of the Dragonfly Agent to that it can modify its
 * behavior.
 */
abstract class InjectionHook {

    /**
     * The name of this hook.
     */
    abstract val name: String

    /**
     * A simple name that can be used for directories to identify the hook.
     */
    abstract val simpleName: String

    /**
     * Configures the Java Instrumentation instance. This function allows the hook to inject
     * its tweakers and other bytecode transformers.
     */
    abstract fun InstrumentationWrapper.configure()

    /**
     * This function is called immediately after the Dragonfly Agent has been
     * attached. It can be used to configure it before it starts it's setup
     * process for example to change the logging provider.
     */
    open fun premain(agent: DragonflyAgent) {}

    /**
     * Called when the hook is loaded into the [agent].
     */
    open fun loadIntoAgent(agent: DragonflyAgent) {}

    /**
     * Returns a more readable [name] of the tweaker to be distinguishable from other
     * parts of a log message.
     */
    val qualifiedName: String by lazy { "<$name>" }
}