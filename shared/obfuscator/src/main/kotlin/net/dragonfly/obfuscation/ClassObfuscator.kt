package net.dragonfly.obfuscation

import net.dragonfly.obfuscation.mapping.ClassMapping
import net.dragonfly.obfuscation.specification.ClassSpecification

class ClassObfuscator : Obfuscator.EntityObfuscator<ClassSpecification, ClassMapping> {

    override val mappings = mutableListOf<ClassMapping>()

    override fun findMapping(spec: ClassSpecification): ClassMapping? =
        mappings.toList().firstOrNull { spec.className == it.deobfuscated.toDotSeparated() }

    override fun findReverseMapping(spec: ClassSpecification): ClassMapping? =
        mappings.toList().firstOrNull { spec.className == it.obfuscated.toDotSeparated() }

    override fun createObfuscatedSpec(map: ClassMapping) = ClassSpecification(
        className = map.obfuscated.toDotSeparated()
    )

    override fun createDeobfuscatedSpec(map: ClassMapping) = ClassSpecification(
        className = map.deobfuscated.toDotSeparated()
    )
}