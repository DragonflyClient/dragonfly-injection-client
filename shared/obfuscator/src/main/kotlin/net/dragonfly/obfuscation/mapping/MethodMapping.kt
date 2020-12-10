package net.dragonfly.obfuscation.mapping

data class MethodMapping(
    val classMapping: ClassMapping,
    val obfuscated: String,
    val obfuscatedDescriptor: MethodDescriptor,
    val deobfuscated: String,
    var deobfuscatedDescriptor: MethodDescriptor,
) {
    fun toStringWithClass() = "${classMapping.obfuscated} -> ${classMapping.deobfuscated}\n\t${toString()}"
    override fun toString() = "$obfuscated$obfuscatedDescriptor -> $deobfuscated$deobfuscatedDescriptor"
}

data class MethodDescriptor(
    val parameters: String,
    val returnType: String,
) {
    override fun toString() = "($parameters)$returnType"
}