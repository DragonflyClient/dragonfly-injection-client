package net.dragonfly.obfuscation.mapping

data class FieldMapping(
    val classMapping: ClassMapping,
    val obfuscated: String,
    val deobfuscated: String,
) {
    fun toStringWithClass() = "${classMapping.obfuscated} -> ${classMapping.deobfuscated}\n\t${toString()}"
    override fun toString() = "$obfuscated -> $deobfuscated"
}