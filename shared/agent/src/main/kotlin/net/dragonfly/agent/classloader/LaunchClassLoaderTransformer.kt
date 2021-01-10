package net.dragonfly.agent.classloader

import javassist.*
import java.lang.instrument.ClassFileTransformer
import java.security.ProtectionDomain

/**
 * Adjusts the `net.minecraft.launchwrapper.LaunchClassLoader` to add the exclusions
 * specified in the [exclusions] list. Classes whose qualified name starts with one
 * of the [exclusions] will be loaded by the original AppClassLoader instead of the
 * LaunchClassLoader.
 */
class LaunchClassLoaderTransformer(
    exclusionsConfiguration: MutableList<String>.() -> Unit,
) : ClassFileTransformer {

    private val exclusions = mutableListOf<String>().apply(exclusionsConfiguration)

    override fun transform(
        loader: ClassLoader, className: String, classBeingRedefined: Class<*>?, protectionDomain: ProtectionDomain?,
        classfileBuffer: ByteArray,
    ): ByteArray {
        if (className == "net/minecraft/launchwrapper/LaunchClassLoader") {
            try {
                println("[Preparation] Transforming net.minecraft.launchwrapper.LaunchClassLoader")
                val cp = ClassPool.getDefault()
                cp.appendClassPath(LoaderClassPath(loader))
                val ct = cp.get(className.replace("/", "."))!!
                val constructor = ct.constructors.first()

                constructor.insertAfter("{${
                    exclusions.joinToString("\n") {
                        """this.addClassLoaderExclusion("$it");"""
                    }
                }}".trimIndent())

                return ct.toBytecode().also { ct.detach() }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        return classfileBuffer
    }
}