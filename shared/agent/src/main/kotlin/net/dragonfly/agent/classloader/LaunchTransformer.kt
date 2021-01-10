package net.dragonfly.agent.classloader

import javassist.*
import net.dragonfly.agent.DragonflyAgent
import net.minecraft.launchwrapper.LaunchClassLoader
import java.lang.instrument.ClassFileTransformer
import java.security.ProtectionDomain

/**
 * Adjusts the `net.minecraft.launchwrapper.Launch` class to use the [LaunchClassLoader]
 * created by Dragonfly [ClassLoaderManager.launchClassLoader] when tweaking the Minecraft
 * classes.
 */
class LaunchTransformer : ClassFileTransformer {

    override fun transform(
        loader: ClassLoader, className: String, classBeingRedefined: Class<*>?, protectionDomain: ProtectionDomain?,
        classfileBuffer: ByteArray,
    ): ByteArray {
        if (className == "net/minecraft/launchwrapper/Launch") {
            try {
                DragonflyAgent.getInstance().log("Transforming net.minecraft.launchwrapper.Launch")
                val cp = ClassPool.getDefault()
                cp.appendClassPath(LoaderClassPath(loader))
                val ct = cp.get(className.replace("/", "."))!!
                val constructor = ct.declaredConstructors.first()

                constructor.setBody("""
                    {
                        classLoader = net.dragonfly.agent.classloader.ClassLoaderManager.getLaunchClassLoader();
                        blackboard = new java.util.HashMap();
                        net.minecraft.launchwrapper.LogWrapper.info("Using injected LaunchClassLoader from Dragonfly", new Object[0]);
                    }
                """.trimIndent())

                return ct.toBytecode().also { ct.detach() }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        return classfileBuffer
    }
}