package net.dragonfly.obfuscation

import net.dragonfly.obfuscation.mapping.*
import java.io.File
import kotlin.concurrent.thread

object Obfuscator {
    private var classObfuscator = ClassObfuscator()
    private var fieldObfuscator = FieldObfuscator()
    private var methodObfuscator = MethodObfuscator()

    var finishedParsing = false
        private set

    fun parseMappings(file: File) = thread(name = "mappings parser", start = true) {
        val lines = file.readLines()

        classObfuscator.mappings.clear()
        fieldObfuscator.mappings.clear()
        methodObfuscator.mappings.clear()

        var currentClass: ClassMapping? = null
        var badLines = 0

        for (line in lines) {
            val split = line.removePrefix("\t").split(" ")
            if (!line.startsWith("\t")) {
                currentClass = ClassMapping(obfuscated = split[0], deobfuscated = split[1])
                classObfuscator.mappings.add(currentClass)
            } else if (split.size == 2) {
                fieldObfuscator.mappings.add(
                    FieldMapping(classMapping = currentClass!!, obfuscated = split[0], deobfuscated = split[1])
                )
            } else if (split.size == 3) {
                val obfParams = split[1].substringBefore(')').removePrefix("(")
                val obfReturn = split[1].substringAfter(')')
                val obfDescriptor = MethodDescriptor(obfParams, obfReturn)

                methodObfuscator.mappings.add(
                    MethodMapping(currentClass!!, split[0], obfDescriptor, split[2], obfDescriptor)
                )
            } else {
                badLines++
            }
        }

        methodObfuscator.mappings.forEach { method ->
            val deobfParams = method.obfuscatedDescriptor.parameters.deobfuscateTypes()
            val deobfReturn = method.obfuscatedDescriptor.returnType.deobfuscateTypes()
            method.deobfuscatedDescriptor = MethodDescriptor(deobfParams, deobfReturn)
        }

        if (badLines > 0)
            println("> Skipped $badLines bad lines while parsing mappings")

        println("> Finished parsing mappings")
        finishedParsing = true
    }

    fun classes() = classObfuscator
    fun fields() = fieldObfuscator
    fun methods() = methodObfuscator

    private fun String.deobfuscateTypes() = replace(Regex("L.*;")) { match ->
        val typeName = match.value.removePrefix("L").removeSuffix(";")
        val deobfTypeName = classObfuscator.mappings.firstOrNull { it.obfuscated == typeName }?.deobfuscated ?: typeName
        "L$deobfTypeName;"
    }

    interface EntityObfuscator<S, M> {
        val mappings: MutableList<M>

        fun createSpec(map: M): S
        fun findMapping(spec: S): M?

        /**
         * Returns null if there is no obfuscation mapping for the given [spec].
         */
        fun obfuscateOrNull(spec: S): S? {
            return awaitMapping(spec)?.let { createSpec(it) }
        }

        /**
         * Returns the given [spec] itself if there is no obfuscation mapping for it.
         */
        fun obfuscate(spec: S) = obfuscateOrNull(spec) ?: spec

        private fun awaitMapping(spec: S): M? {
            var found: M? = findMapping(spec)
            while (!finishedParsing && found == null) {
                found = findMapping(spec)
                Thread.sleep(5)
            }

            return found
        }

        fun String.toSlashSeparated() = replace(".", "/")
        fun String.toDotSeparated() = replace("/", ".")
    }
}