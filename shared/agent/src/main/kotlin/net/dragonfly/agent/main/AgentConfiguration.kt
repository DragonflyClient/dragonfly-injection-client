package net.dragonfly.agent.main

import com.xenomachina.argparser.ArgParser

/**
 * The configuration for the Dragonfly Agent which is parsed using the `kotlin-argparser`
 * library.
 *
 * The arguments must be set using the following syntax by replacing `<ARGUMENTS>` with
 * the actual arguments that configure the agent:
 * ```
 * java -javaagent:dragonfly-agent.jar="<ARGUMENTS>" -jar ...
 * ```
 */
class AgentConfiguration(argParser: ArgParser) {

    /**
     * The collection that contains the bootstrap classes of the injection hooks.
     */
    val bootstrapClasses by argParser.adding(
        "-i", "--injection-hook",
        help = "bootstrap class of an injection hook to enable"
    )

    /**
     * The Minecraft version that this agent is attached to.
     */
    val version by argParser.storing(
        "-v", "--version",
        help = "version of Minecraft into which Dragonfly is injected"
    )

    /**
     * Whether the agent should exit when a hook fails to load.
     */
    val requireAllHooks by argParser.flagging(
        "-A", "--require-all-hooks",
        help = "whether the agent should exit when a hook fails to load"
    )
}