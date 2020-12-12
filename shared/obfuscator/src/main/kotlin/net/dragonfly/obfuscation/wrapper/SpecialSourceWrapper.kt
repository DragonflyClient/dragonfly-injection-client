package net.dragonfly.obfuscation.wrapper

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import net.dragonfly.obfuscation.Obfuscator
import net.dragonfly.obfuscation.mapping.*
import java.io.File
import kotlin.system.exitProcess

object SpecialSourceWrapper {

    @JvmStatic
    fun main(input: Array<String>) = mainBody {
        val args = ArgParser(input).parseInto(::WrapperArguments)

        println("== Dragonfly Obfuscation API ==")

        if (!File(args.inputJar).exists()) {
            println("! Input JAR file doesn't exist")
            exitProcess(404)
        }

        if (!args.reversedMappings.exists()) {
            println("> Creating reversed mappings from index file ${args.indexFile}")

            if (!args.indexFile.exists()) {
                println("! Index file doesn't exist")
                exitProcess(404)
            }

            createReversedSeargeMappings(args.indexFile, args.reversedMappings)
            println("> Reversed mappings created")
        } else {
            println("> Using reversed mappings from existing file ${args.reversedMappings}")
        }

        println("== Launching SpecialSource ==")
        println("> Path: ${args.specialSource}")
        println("> Input JAR: ${args.inputJar}")
        println("> Output JAR: ${args.outputJar}")

        println("== SpecialSource output ==")
        val process = ProcessBuilder()
            .command("java",
                "-cp", args.minecraftJar + ";" + args.specialSource,
                "net.md_5.specialsource.SpecialSource",
                "--in-jar", args.inputJar,
                "--out-jar", args.outputJar,
                "--srg-in", args.reversedMappings.absolutePath,
                "--live"
            )
            .directory(File("."))
            .inheritIO()
            .start()

        val exitCode = process.waitFor()
        println("== End of SpecialSource output ==")

        if (exitCode != 0) {
            println("! Error: Process finished with exit code $exitCode")
        } else {
            println("+ Done")
        }
    }

    private fun createReversedSeargeMappings(indexFile: File, seargeFile: File) = Obfuscator.run {
        mutableListOf<String>().apply {
            parseMappings(indexFile)

            while (!finishedParsing) {
                Thread.sleep(5)
            }

            classes().mappings.forEach(::classLine)
            fields().mappings.forEach(::fieldLine)
            methods().mappings.forEach(::methodLine)
        }.let { seargeFile.writeText(it.joinToString("\n")) }
    }
}

private fun MutableList<String>.classLine(it: ClassMapping) =
    add("CL: ${it.deobfuscated} ${it.obfuscated}")

private fun MutableList<String>.fieldLine(it: FieldMapping) =
    add("FD: ${it.classMapping.deobfuscated}/${it.deobfuscated} ${it.classMapping.obfuscated}/${it.obfuscated}")

private fun MutableList<String>.methodLine(it: MethodMapping) =
    add("MD: ${it.classMapping.deobfuscated}/${it.deobfuscated} ${it.deobfuscatedDescriptor} " +
            "${it.classMapping.obfuscated}/${it.obfuscated} ${it.obfuscatedDescriptor}")