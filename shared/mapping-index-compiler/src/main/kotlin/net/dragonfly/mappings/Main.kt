package net.dragonfly.mappings

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.xenomachina.argparser.*
import java.io.File

object Main {
    lateinit var jackson: ObjectMapper
    lateinit var provider: MappingIndicesProvider

    lateinit var compiler: MappingIndexCompiler
    lateinit var programArguments: ProgramArguments

    @JvmStatic
    fun main(args: Array<String>) {
        println("=== Dragonfly Mapping Index Compiler ===")
        programArguments = ArgParser(args).parseInto(::ProgramArguments)

        jackson = jacksonObjectMapper()
        provider = MappingIndicesProvider()
        compiler = MappingIndexCompiler(programArguments.destinationDir, programArguments.tempDir)

        provider.get(programArguments.version)
            ?.let { compiler.compile(it) }
            ?: println("! No mappings index found for version ${programArguments.version}")
    }
}