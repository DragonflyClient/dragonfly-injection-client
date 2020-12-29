package net.dragonfly.agent.transformer

import net.dragonfly.agent.DragonflyAgent
import net.dragonfly.agent.hook.Level
import net.dragonfly.agent.tweaker.*
import org.objectweb.asm.*
import org.objectweb.asm.tree.*
import java.lang.instrument.ClassFileTransformer
import java.security.ProtectionDomain
import kotlin.reflect.KClass

class TweakTransformer(
    private val tweaker: Tweaker,
) : ClassFileTransformer {

    private val targetClassName = tweaker.targetClass.obfuscated.className
    private val targetFileName = targetClassName.replace(".", "/")

    override fun transform(
        loader: ClassLoader, className: String, classBeingRedefined: Class<*>?,
        protectionDomain: ProtectionDomain, classfileBuffer: ByteArray,
    ): ByteArray {
        if (className != targetFileName) {
            return classfileBuffer
        }

        try {
            DragonflyAgent.getInstance().log("> Applying tweak transformer ${tweaker::class.simpleName} to $targetClassName")

            val src = ClassNode()
            val dest = ClassNode()

            ClassReader(tweaker::class.java.name.replace(".", "/")).accept(src, 0)
            ClassReader(classfileBuffer).accept(dest, 0)

            return TweakApplier.create(tweaker, src, dest)
                .runMethodSubstitution()
                .runMethodInjection()
                .runFieldInjection()
                .runRedirection()
                .writeClass()
        } catch (e: Exception) {
            DragonflyAgent.getInstance().log("! Error transforming class $targetClassName!", Level.ERROR)
            DragonflyAgent.getInstance().log(e.stackTraceToString(), Level.ERROR)
        }

        return classfileBuffer
    }
}

fun MethodNode.hasAnnotation(kclass: KClass<*>) =
    visibleAnnotations?.any { it.desc == Type.getDescriptor(kclass.java) } == true

fun FieldNode.hasAnnotation(kclass: KClass<*>) =
    visibleAnnotations?.any { it.desc == Type.getDescriptor(kclass.java) } == true
