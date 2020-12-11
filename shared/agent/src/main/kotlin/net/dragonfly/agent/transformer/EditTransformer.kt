package net.dragonfly.agent.transformer

import net.dragonfly.agent.dsl.specification
import javassist.ClassPool
import javassist.CtClass
import net.dragonfly.agent.DragonflyAgent
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
            DragonflyAgent.getInstance().log("> Applying edit transformer to $targetClassName")

            val classPool = ClassPool.getDefault()
            val ct = classPool.get(targetClassName)!!

            ct.specification = classSpecification
            ct.apply(editFunction)

            return ct.toBytecode().also { ct.detach() }
        } catch (e: Exception) {
            DragonflyAgent.getInstance().log("! Error transforming class $targetClassName")
            DragonflyAgent.getInstance().log(e.stackTraceToString())
        }

        return classfileBuffer
    }
}
