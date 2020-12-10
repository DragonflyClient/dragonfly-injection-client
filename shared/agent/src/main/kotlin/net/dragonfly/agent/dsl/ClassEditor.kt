package net.dragonfly.agent.dsl

import javassist.*
import net.dragonfly.obfuscation.specification.ClassSpecification
import net.dragonfly.obfuscation.specification.MethodSpecification

fun CtClass.getDeclaredMethod(method: MethodSpecification): CtMethod =
    getDeclaredMethods(method.methodName)
        .firstOrNull { method.descriptor == null || it.signature == method.descriptor }
        ?: throw IllegalArgumentException("No method found matching specification $method!")

fun CtClass.editMethod(targetMethodName: String, block: CtMethod.() -> Unit = {}): CtMethod {
    val method = MethodSpecification(specification!!.className, targetMethodName).obfuscated
    return getDeclaredMethod(method).apply(block)
}

fun CtClass.createMethod(src: String): CtMethod =
    CtMethod.make(src, this).also { this.addMethod(it) }

var CtClass.specification: ClassSpecification?
    set(value) {
        if (value != null)
        ctClassSpecifications[this] = value
    }
    get() = ctClassSpecifications[this]

private val ctClassSpecifications = mutableMapOf<CtClass, ClassSpecification>()