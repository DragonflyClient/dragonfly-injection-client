package net.dragonfly.agent.transformer

import net.dragonfly.agent.DragonflyAgent
import net.dragonfly.agent.hook.Level
import net.dragonfly.agent.tweaker.*
import org.objectweb.asm.*
import org.objectweb.asm.tree.*
import java.io.File
import java.lang.instrument.ClassFileTransformer
import java.security.ProtectionDomain
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmName

class TweakTransformer(
    private val tweaker: Tweaker,
) : ClassFileTransformer {

    private val targetClassName = tweaker.targetClass.obfuscated.className
    private val targetFileName = targetClassName.replace(".", "/")

    override fun transform(
        loader: ClassLoader, className: String, classBeingRedefined: Class<*>?,
        protectionDomain: ProtectionDomain, classfileBuffer: ByteArray,
    ): ByteArray {
        try {
            if (className == targetFileName) {
                DragonflyAgent.getInstance().log("> Applying tweaker ${tweaker::class.simpleName} to $targetClassName")

                val src = ClassNode()
                val dest = ClassNode()

                val srcReader = ClassReader(tweaker::class.java.name.replace(".", "/"))
                srcReader.accept(src, 0)
                val destReader = ClassReader(classfileBuffer)
                destReader.accept(dest, 0)

                return TweakApplier.create(tweaker, src, dest)
                    .runMethodSubstitution()
                    .runMethodInjection()
                    .runFieldInjection()
                    .runRedirection()
                    .debug()
                    .writeClass()
            } else if (tweaker::class.jvmName.replace(".", "/").let { className.startsWith(it) && className[it.length] == '$' }) {
                DragonflyAgent.getInstance().log("> Transforming inner class of tweaker ${tweaker::class.simpleName}: $className")

                val dest = ClassNode().also { ClassReader(classfileBuffer).accept(it, 0) }
                dest.access = Opcodes.ACC_PUBLIC or Opcodes.ACC_FINAL
                dest.methods.filter { it.name == "<init>" }.forEach { it.access = Opcodes.ACC_PUBLIC }

                return ClassWriter(0).also { dest.accept(it) }.toByteArray().also {
                    val outputFile = File("classes\\" + className.replace("/", ".") + ".class")
                    outputFile.writeBytes(it)
                }
            }
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
