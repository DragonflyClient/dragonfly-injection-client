package net.dragonfly.obfuscation

import net.dragonfly.obfuscation.mapping.FieldMapping
import net.dragonfly.obfuscation.specification.FieldSpecification

class FieldObfuscator : Obfuscator.EntityObfuscator<FieldSpecification, FieldMapping> {

    override val mappings = mutableListOf<FieldMapping>()

    override fun findMapping(spec: FieldSpecification): FieldMapping? =
        mappings.toList().filter { it.classMapping.deobfuscated == spec.clazz.toSlashSeparated() }
            .firstOrNull { it.deobfuscated == spec.fieldName }

    override fun createSpec(map: FieldMapping) = FieldSpecification(
        clazz = map.classMapping.obfuscated.toDotSeparated(),
        fieldName = map.obfuscated
    )
}