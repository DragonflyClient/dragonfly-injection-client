package net.dragonfly.agent.classloader

import javassist.*
import javassist.expr.ConstructorCall
import javassist.expr.ExprEditor
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
                val cp = ClassPool.getDefault().also { it.appendClassPath(LoaderClassPath(loader)) }
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

    /**
     * When using an OptiFine Launch Wrapper version prior to 2.2 new Java versions will
     * prevent the socket connection to some (randomly selected?) servers. This function
     * transforms the LaunchCLassLoader class to fix this issue.
     *
     * Credits go to ZekerZhayard for his [pull request](https://github.com/sp614x/LegacyLauncher/pull/1)
     * on the OptiFine Launch Wrapper GitHub repository.
     *
     * > **Note**: Since the current (12/JAN/2020) version of the Dragonfly Launcher is using
     * OptiFine Launch Wrapper v2.2, this transformation is redundant and - in fact - will
     * cause an error since the `getParentClassLoader()` method already exists in this version.
     */
    private fun fixLaunchClassLoader(ct: CtClass) {
        // create getParentClassLoader() method which is invoked in the super-call
        CtMethod.make("""
            private static ClassLoader getParentClassLoader() {
                if (!System.getProperty("java.version").startsWith("1.")) {
                    try {
                        return (ClassLoader) ClassLoader.class.getDeclaredMethod("getPlatformClassLoader", new Class[0]).invoke(null, new Object[0]);
                    } catch (Throwable t) {
                        net.minecraft.launchwrapper.LogWrapper.warning("No platform classloader: " + System.getProperty("java.version"), new Object[0]);
                    }
                }
                return null;
            }
        """.trimIndent(), ct).also { ct.addMethod(it) }

        // replace super call to invoke the above method
        ct.constructors.first().instrument(object : ExprEditor() {
            override fun edit(c: ConstructorCall) {
                if (c.isSuper) {
                    c.replace("super(sources, getParentClassLoader());")
                }
            }
        })
    }
}