package net.dragonfly.agent.tweaker

import net.dragonfly.obfuscation.specification.ClassSpecification

open class Tweaker(val targetClass: ClassSpecification) {
    constructor(className: String) : this(ClassSpecification(className))
}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class SubstituteMethod

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class CopyMethod

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class Remap