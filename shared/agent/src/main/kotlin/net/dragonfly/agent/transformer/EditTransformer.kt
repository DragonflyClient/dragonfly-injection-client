package net.dragonfly.agent.transformer

import net.dragonfly.agent.dsl.specification
import net.dragonfly.agent.log
import javassist.ClassPool
import javassist.CtClass
import net.dragonfly.obfuscation.specification.ClassSpecification
import java.lang.instrument.ClassFileTransformer
import java.security.ProtectionDomain

class EditTransformer(
    private val classSpecification: ClassSpecification,
    private val editFunction: CtClass.() -> Unit,
) : ClassFileTransformer {

    private val targetClassName = classSpecification.className
    private val targetFileName = targetClassName.replace(".", "/")

    override fun transform(
        loader: ClassLoader, className: String, classBeingRedefined: Class<*>?,
        protectionDomain: ProtectionDomain, classfileBuffer: ByteArray
    ): ByteArray {
        if (className != targetFileName) {
            return classfileBuffer
        }

        try {
            log("> Applying edit transformer to $targetClassName")

            val classPool = ClassPool.getDefault()
            val ct = classPool.get(targetClassName)!!

            ct.specification = classSpecification
            ct.apply(editFunction)

            return ct.toBytecode().also { ct.detach() }
        } catch (e: Exception) {
            log("! Error transforming class $targetClassName")
            log(e.stackTraceToString())
        }

        return classfileBuffer
    }
}
