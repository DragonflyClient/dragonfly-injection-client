package net.dragonfly.obfuscation

import net.dragonfly.obfuscation.mapping.MethodMapping
import net.dragonfly.obfuscation.specification.MethodSpecification

class MethodObfuscator : Obfuscator.EntityObfuscator<MethodSpecification, MethodMapping> {

    override val mappings = mutableListOf<MethodMapping>()

    override fun findMapping(spec: MethodSpecification): MethodMapping? =
        mappings.toList().filter { it.deobfuscated == spec.clazz.toSlashSeparated() }
            .filter { it.deobfuscated == spec.methodName }
            .firstOrNull { spec.descriptor == null || it.deobfuscatedDescriptor.toString() == spec.descriptor }

    override fun createSpec(map: MethodMapping) = MethodSpecification(
        clazz = map.classMapping.obfuscated.toDotSeparated(),
        methodName = map.obfuscated,
        descriptor = map.obfuscatedDescriptor.toString()
    )
}