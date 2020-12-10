package net.dragonfly.obfuscation.specification

import net.dragonfly.obfuscation.Obfuscator

data class FieldSpecification(
    val clazz: String,
    val fieldName: String
) {
    val obfuscated get() = Obfuscator.fields().obfuscate(this)

    override fun toString() = "${clazz}.$fieldName"
}