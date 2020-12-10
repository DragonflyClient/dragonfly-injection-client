package net.dragonfly.obfuscation

import net.dragonfly.obfuscation.mapping.ClassMapping
import net.dragonfly.obfuscation.specification.ClassSpecification

class ClassObfuscator : Obfuscator.EntityObfuscator<ClassSpecification, ClassMapping> {

    override val mappings = mutableListOf<ClassMapping>()

    override fun findMapping(spec: ClassSpecification): ClassMapping? =
        mappings.toList().firstOrNull { spec.className == it.deobfuscated.toDotSeparated() }

    override fun createSpec(map: ClassMapping) = ClassSpecification(
        className = map.obfuscated.toDotSeparated()
    )
}