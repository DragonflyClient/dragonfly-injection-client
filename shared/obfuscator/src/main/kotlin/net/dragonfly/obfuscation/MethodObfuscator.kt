package net.dragonfly.obfuscation

import net.dragonfly.obfuscation.mapping.MethodMapping
import net.dragonfly.obfuscation.specification.MethodSpecification

class MethodObfuscator : Obfuscator.EntityObfuscator<MethodSpecification, MethodMapping> {

    override val mappings = mutableListOf<MethodMapping>()

    override fun findMapping(spec: MethodSpecification): MethodMapping? =
        mappings.toList().filter { it.classMapping.deobfuscated == spec.clazz.toSlashSeparated() }
            .filter { it.deobfuscated == spec.methodName }
            .firstOrNull { spec.descriptor == null || it.deobfuscatedDescriptor.toString() == spec.descriptor }

    override fun findReverseMapping(obfSpec: MethodSpecification): MethodMapping? =
        mappings.toList().filter { it.classMapping.obfuscated == obfSpec.clazz.toSlashSeparated() }
            .filter { it.obfuscated == obfSpec.methodName }
            .firstOrNull { obfSpec.descriptor == null || it.obfuscatedDescriptor.toString() == obfSpec.descriptor }

    override fun createObfuscatedSpec(map: MethodMapping) = MethodSpecification(
        clazz = map.classMapping.obfuscated.toDotSeparated(),
        methodName = map.obfuscated,
        descriptor = map.obfuscatedDescriptor.toString()
    )

    override fun createDeobfuscatedSpec(map: MethodMapping) = MethodSpecification(
        clazz = map.classMapping.deobfuscated.toDotSeparated(),
        methodName = map.deobfuscated,
        descriptor = map.deobfuscatedDescriptor.toString()
    )
}