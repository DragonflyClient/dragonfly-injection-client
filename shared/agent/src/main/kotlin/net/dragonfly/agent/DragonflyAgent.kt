@file:Suppress("CanBeParameter", "unused", "MemberVisibilityCanBePrivate")

package net.dragonfly.agent

import net.dragonfly.agent.dsl.InstrumentationWrapper
import net.dragonfly.agent.hook.*
import net.dragonfly.agent.main.AgentConfiguration
import net.dragonfly.obfuscation.*
import java.io.File
import java.lang.instrument.Instrumentation
import kotlin.IllegalStateException
import kotlin.system.exitProcess

/**
 * The core class of the Dragonfly Agent. It is instantiated in the `premain` method
 * after the [configuration] has been parsed. The instances of this class are managed
 * by its companion object which makes sure that never more than 1 instance of this
 * class exist.
 *
 * @param configuration the configuration that controls the behavior of the agent
 * @param instrumentation Java Instrumentation to transform the bytecode
 */
class DragonflyAgent private constructor(
    val configuration: AgentConfiguration,
    val instrumentation: Instrumentation,
) {
    /** Minecraft version that this agent is attached to */
    val minecraftVersion = configuration.version
    /** Collection of bootstrap classes of the injection hooks */
    val bootstrapClasses = configuration.bootstrapClasses

    /** Wrapper around the [Instrumentation] class to enable the DSL */
    val instrumentationWrapper = InstrumentationWrapper(instrumentation, this)
    /** Collection of loaded injection hooks */
    val injectionHooks = mutableListOf<InjectionHook>()

    /** The message provider that is used by the agent to publish log messages */
    var loggingProvider: LoggingProvider = DefaultLoggingProvider

    /**
     * Sets up the agent by first calling the `premain` functions of all [bootstrapClasses],
     * preparing the obfuscator and then loading the injection hooks.
     */
    private fun setup() {
        callPremains()

        log("=== Dragonfly Agent ===")
        log("> Setting up agent for Minecraft $minecraftVersion")

        prepareObfuscator()
        loadInjectionHooks()
    }

    /**
     * Tells the [Obfuscator] to start parsing the mappings from the default mapping
     * index file that depends on the [minecraftVersion].
     */
    private fun prepareObfuscator() {
        log("> Parsing obfuscation mappings...")
        Obfuscator.parseMappings(File("dragonfly\\mappings\\$minecraftVersion\\index.pack")) {
            log("> Obfuscation mappings have been fully loaded!")
        }
    }

    /**
     * Calls the optional `premain` functions on all [bootstrapClasses].
     *
     * This function does not show any errors if a class does not exist or if an error occurs
     * during the function call.
     */
    private fun callPremains() {
        bootstrapClasses.forEach {
            it.runCatching {
                (Class.forName(this).kotlin.objectInstance as InjectionHook).premain(this@DragonflyAgent)
            }
        }
    }

    /**
     * Resolves the [bootstrapClasses] and invokes the [InjectionHook.loadIntoAgent] as well
     * as the [InjectionHook.configure] functions.
     *u
     * If an error occurs during the loading process, this hook is ignored and the process
     * continues. Due to this behavior, the [injectionHooks] list might be incomplete. If
     * you require all hooks to be loaded, you can specify this behavior in the agent
     * configuration ([AgentConfiguration.requireAllHooks]).
     */
    private fun loadInjectionHooks() {
        log("=== Loading Injection Hooks ===")

        for (bootstrap in bootstrapClasses) {
            try {
                val clazz = Class.forName(bootstrap).kotlin

                log("== $bootstrap ==")
                val hook = clazz.objectInstance as InjectionHook

                log("> Name of hook is: ${hook.name}")
                hook.loadIntoAgent(this)

                with(hook) {
                    instrumentationWrapper.configure()
                }

                injectionHooks.add(hook)
                log("> Successfully loaded")
                continue
            } catch (e: ClassNotFoundException) {
                log("! Could not resolve bootstrap class $bootstrap")
            } catch (e: Throwable) {
                log("! Could not load injection hook $bootstrap: $e")
            }

            if (configuration.requireAllHooks) {
                exitProcess(300)
            }
        }
    }

    /**
     * Logs the [message] with the [level] using the [loggingProvider].
     */
    fun log(message: String, level: Level = Level.INFO) {
        loggingProvider.sendLog(message, level)
    }

    companion object {
        /**
         * The instance of the Dragonfly Agent if it is executed as a Java Agent or null if no
         * Dragonfly Agent has been created.
         */
        private var instance: DragonflyAgent? = null

        /**
         * Returns the [instance] of the Dragonfly Agent or throws an exception if the agent
         * has not been executed as a Java Agent.
         *
         * @throws IllegalStateException no instance of the Dragonfly Agent exists
         */
        fun getInstance(): DragonflyAgent = instance
            ?: throw IllegalStateException("The Dragonfly Agent has not been attached to a JVM")

        /**
         * Creates the Dragonfly Agent by calling its constructor with the given parameters
         * or throws an exception of an instance of the agent already exists.
         *
         * @throws IllegalStateException an instance of the Dragonfly Agent already exists
         */
        @Synchronized
        fun create(configuration: AgentConfiguration, instrumentation: Instrumentation) {
            if (instance != null) throw IllegalStateException("The Dragonfly Agent has already been initialized")
            instance = DragonflyAgent(configuration, instrumentation)
            instance!!.setup()
        }
    }
}