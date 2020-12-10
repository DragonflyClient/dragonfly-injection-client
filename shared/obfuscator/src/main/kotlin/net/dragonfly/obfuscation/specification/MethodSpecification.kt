package net.dragonfly.obfuscation.specification

import net.dragonfly.obfuscation.Obfuscator

data class MethodSpecification(
    val clazz: String,
    val methodName: String,
    val descriptor: String? = null
) {
    val obfuscated get() = Obfuscator.methods().obfuscate(this)

    override fun toString() = "${clazz}.$methodName${descriptor ?: ""}"
}