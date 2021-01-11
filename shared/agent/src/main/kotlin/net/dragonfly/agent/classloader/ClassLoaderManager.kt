package net.dragonfly.agent.classloader

import net.minecraft.launchwrapper.LaunchClassLoader
import net.minecraft.launchwrapper.Launch
import java.lang.instrument.Instrumentation

import java.io.File
import java.net.*

/**
 * This class handles the switching between the default JDK 15 AppClassLoader and the
 * LaunchClassLoader that is provided by the Mojang LegacyLauncher system and used by
 * OptiFine to tweak the Minecraft classes.
 */
object ClassLoaderManager {

    /**
     * The instance of the LaunchClassLoader that will be used by the LegacyLauncher.
     */
    @JvmStatic
    lateinit var launchClassLoader: LaunchClassLoader

    /**
     * Initializes the [launchClassLoader] and changes the [Thread.contextClassLoader]
     * to the new one.
     */
    fun switchContextClassLoader() {
        println("[Preparation] Switching context class loader")

        launchClassLoader = LaunchClassLoader(getURLs())
        Thread.currentThread().contextClassLoader = launchClassLoader
    }

    /**
     * Registers the transformers on the [instrumentation] instance that tweak the
     * [Launch] class to use the [launchClassLoader] by Dragonfly and the [LaunchClassLoader]
     * to add class loader exclusions.
     */
    fun registerTransformer(instrumentation: Instrumentation) {
        instrumentation.addTransformer(LaunchClassLoaderTransformer {
            add("net.dragonfly.agent.")
            add("org.koin.")
            add("kotlin.")
            add("javassist.")
        })
        instrumentation.addTransformer(LaunchTransformer())
    }

    /**
     * Returns the URLs of the current Java class path.
     */
    private fun getURLs(): Array<URL?> {
        val cp = System.getProperty("java.class.path")
        var elements = cp.split(File.pathSeparator).toTypedArray()
        if (elements.isEmpty()) {
            elements = arrayOf("")
        }
        val urls: Array<URL?> = arrayOfNulls(elements.size)
        for (i in elements.indices) {
            try {
                val url: URL = File(elements[i]).toURI().toURL()
                urls[i] = url
            } catch (ex: MalformedURLException) {
            }
        }
        return urls
    }
}