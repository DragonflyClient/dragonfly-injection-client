package net.dragonfly.obfuscation

import net.dragonfly.obfuscation.mapping.FieldMapping
import net.dragonfly.obfuscation.specification.FieldSpecification

class FieldObfuscator : Obfuscator.EntityObfuscator<FieldSpecification, FieldMapping> {

    override val mappings = mutableListOf<FieldMapping>()

    override fun findMapping(spec: FieldSpecification): FieldMapping? =
        mappings.toList().filter { it.classMapping.deobfuscated == spec.clazz.toSlashSeparated() }
            .firstOrNull { it.deobfuscated == spec.fieldName }

    override fun findReverseMapping(obfSpec: FieldSpecification): FieldMapping? =
        mappings.toList().filter { it.classMapping.obfuscated == obfSpec.clazz.toSlashSeparated() }
            .firstOrNull { it.obfuscated == obfSpec.fieldName }

    override fun createObfuscatedSpec(map: FieldMapping) = FieldSpecification(
        clazz = map.classMapping.obfuscated.toDotSeparated(),
        fieldName = map.obfuscated
    )

    override fun createDeobfuscatedSpec(map: FieldMapping) = FieldSpecification(
        clazz = map.classMapping.deobfuscated.toDotSeparated(),
        fieldName = map.deobfuscated
    )
}