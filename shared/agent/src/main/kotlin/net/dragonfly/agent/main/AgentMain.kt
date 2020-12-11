package net.dragonfly.agent.main

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import net.dragonfly.agent.DragonflyAgent
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
    fun premain(input: String?, instrumentation: Instrumentation) = mainBody {
        val args = input?.split(" ")?.toTypedArray() ?: arrayOf()
        val config = ArgParser(args).parseInto(::AgentConfiguration)

        DragonflyAgent.create(config, instrumentation)
    }
}