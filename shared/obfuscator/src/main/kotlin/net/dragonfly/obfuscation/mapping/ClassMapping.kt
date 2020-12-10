package net.dragonfly.obfuscation.mapping

data class ClassMapping(
    val obfuscated: String,
    val deobfuscated: String,
) {
    override fun toString() = "$obfuscated -> $deobfuscated"
}