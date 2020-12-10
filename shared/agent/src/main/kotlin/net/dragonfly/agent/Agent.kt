package net.dragonfly.agent

import net.dragonfly.agent.dsl.InstrumentationWrapper
import net.dragonfly.obfuscation.*
import java.io.File
import java.lang.instrument.Instrumentation

abstract class Agent(
    private val arguments: String?,
    private val instrumentation: Instrumentation,
) {
    val version = arguments.takeUnless { it.isNullOrBlank() }
        ?: "1.8.8".also { println("* Using default version 1.8.8 since no version was specified") }

    init {
        Obfuscator.parseMappings(File("dragonfly\\mappings\\$version\\index.pack"))
    }

    fun initialize() {
        val wrapper = InstrumentationWrapper(instrumentation, this)
        wrapper.build()
    }

    protected abstract fun InstrumentationWrapper.build()
}

fun log(message: String) {
    println(message)
}