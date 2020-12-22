package net.dragonfly.core

import net.dragonfly.agent.tweaker.*

object ClientBrandRetrieverTweaker : Tweaker("net.minecraft.client.ClientBrandRetriever") {

    @JvmStatic
    @Substitute
    fun getClientModName(): String {
        return giveDragonflyName()
    }

    @JvmStatic
    @Inject
    fun giveDragonflyName() = " Dragonfly Tweaker Injected!".toCharArray()
        .let {
            val offset = (System.currentTimeMillis() / 100 % it.size).toInt()
            it.drop(offset) + it.take(offset)
        }
        .drop(1)
        .joinToString("")
}