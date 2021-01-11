package net.dragonfly.agent.main

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import net.dragonfly.agent.DragonflyAgent
import net.dragonfly.agent.classloader.ClassLoaderManager
import java.lang.instrument.Instrumentation

/**
 * The main class of the Dragonfly Agent.
 *
 * The Java Virtual Machine calls the [premain] function on this class when the
 * agent has been attached to a process before this process has started (static
 * attach).
 *
 * Since the Dragonfly Agent only supports static attach, an `agentmain` function
 * is not required.
 */
object AgentMain {

    @JvmStatic
    fun premain(input: String?, instrumentation: Instrumentation): Unit = mainBody {
        with(ClassLoaderManager) {
            registerTransformer(instrumentation)
            switchContextClassLoader()
        }

        val args = input?.split(" ")?.toTypedArray() ?: arrayOf()
        val config = ArgParser(args).parseInto(::AgentConfiguration)

        println("Bootstrapping Dragonfly Agent")
        DragonflyAgent.create(config, instrumentation)
    }
}