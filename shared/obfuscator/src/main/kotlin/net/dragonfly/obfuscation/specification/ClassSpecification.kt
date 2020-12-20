package net.dragonfly.obfuscation.specification

import net.dragonfly.obfuscation.Obfuscator

data class ClassSpecification(
    val className: String,
) {
    val obfuscated get() = Obfuscator.classes().obfuscate(this)
    val deobfuscated get() = Obfuscator.classes().deobfuscate(this)

    override fun toString() = className
}