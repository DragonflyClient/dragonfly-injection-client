package net.dragonfly.agent.tweaker

import net.dragonfly.obfuscation.specification.ClassSpecification
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.annotation.AnnotationTarget.FUNCTION

open class Tweaker(val targetClass: ClassSpecification) {
    constructor(className: String) : this(ClassSpecification(className))
}

@Retention(RUNTIME)
@Target(FUNCTION)
annotation class Substitute

@Retention(RUNTIME)
@Target(FUNCTION, FIELD)
annotation class Inject

@Retention(RUNTIME)
@Target(FUNCTION, FIELD)
annotation class Redirect(
    val origin: Origin = Origin()
)

annotation class Origin(
    val dynamicOrigin: DynamicOrigin = DynamicOrigin.NONE,
    val staticOrigin: String = "."
)

enum class DynamicOrigin {
    NONE,
    SUPERCLASS,
    INTERFACE
}