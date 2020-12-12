package net.dragonfly.agent.transformer

import net.dragonfly.agent.DragonflyAgent
import net.dragonfly.agent.tweaker.*
import net.dragonfly.obfuscation.Obfuscator
import net.dragonfly.obfuscation.mapping.ClassMapping
import net.dragonfly.obfuscation.mapping.FieldMapping
import net.dragonfly.obfuscation.specification.FieldSpecification
import net.dragonfly.obfuscation.specification.MethodSpecification
import org.objectweb.asm.*
import org.objectweb.asm.tree.*
import org.objectweb.asm.util.TraceClassVisitor
import java.io.File
import java.io.PrintWriter
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

            val substituteMethods = src.methods.filter { it.hasAnnotation(SubstituteMethod::class) || it.hasAnnotation(CopyMethod::class) }

            substituteMethods
                .forEach { method ->
                    val spec = MethodSpecification(tweaker.targetClass.className, method.name, method.desc).obfuscated
                    val old = dest.methods.firstOrNull { o -> o.name == spec.methodName && o.desc == spec.descriptor }

                    if (old != null) {
                        DragonflyAgent.getInstance().log("> Substituting method ${dest.name}.${old.name}${old.desc} with ${src.name}.${method.name}")
                        method.access = old.access
                        dest.methods.remove(old)
                    } else {
                        DragonflyAgent.getInstance().log("> Cloning method ${src.name}.${method.name}${method.desc} into ${dest.name}")
                    }

                    method.name = spec.methodName
                    method.desc = spec.descriptor
                    dest.methods.add(method)
                }

            val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES)
            val tweaker = object : ClassVisitor(Opcodes.ASM9, TraceClassVisitor(cw, PrintWriter(File("tweaked.txt")))) {
                override fun visitMethod(
                    access: Int, name: String?, descriptor: String?, signature: String?,
                    exceptions: Array<out String>?,
                ): MethodVisitor? {

                    val general = cv.visitMethod(access, name, descriptor, signature, exceptions)
                    if (substituteMethods.none { it.name == name && it.desc == descriptor })
                        return general

                    val fields = src.fields
                        .filter { it.hasAnnotation(Remap::class) }
                        .map {
                            val spec = FieldSpecification(tweaker.targetClass.className, it.name)
                            Obfuscator.fields().findMapping(spec) ?: FieldMapping(
                                ClassMapping(spec.clazz, spec.clazz),
                                spec.fieldName, spec.fieldName
                            )
                        }

                    return object : MethodVisitor(Opcodes.ASM9, general) {
                        override fun visitFieldInsn(opcode: Int, owner: String?, name: String?, descriptor: String?) {
                            val targetField = fields.firstOrNull {
                                it.deobfuscated == name && tweaker::class.java.name.replace(".", "/") == owner
                            } ?: return super.visitFieldInsn(opcode, owner, name, descriptor)

                            DragonflyAgent.getInstance().log("> Remapping field ${owner}.${name} -> ${targetField.classMapping.obfuscated}.${targetField.obfuscated}")
                            return super.visitFieldInsn(opcode, targetField.classMapping.obfuscated, targetField.obfuscated, descriptor)
                        }

                        override fun visitLocalVariable(
                            name: String?, descriptor: String?, signature: String?, start: Label?, end: Label?,
                            index: Int,
                        ) {
                            if (descriptor == Type.getDescriptor(tweaker::class.java)) {
                                return super.visitLocalVariable(name, "L${dest.name};", signature, start, end, index)
                            }
                            super.visitLocalVariable(name, descriptor, signature, start, end, index)
                        }

                        override fun visitMethodInsn(opcode: Int, owner: String?, name: String?, descriptor: String?, isInterface: Boolean) {
                            if (owner == src.name) {
                                return super.visitMethodInsn(opcode, dest.name, name, descriptor, isInterface)
                            }
                            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
                        }
                    }
                }
            }

            dest.accept(tweaker)

            File("tweaked.class").writeBytes(cw.toByteArray())
            return cw.toByteArray()
        } catch (e: Exception) {
            DragonflyAgent.getInstance().log("! Error transforming class $targetClassName!")
            DragonflyAgent.getInstance().log(e.stackTraceToString())
        }

        return classfileBuffer
    }
}

fun MethodNode.hasAnnotation(kclass: KClass<*>) =
    visibleAnnotations?.any { it.desc == Type.getDescriptor(kclass.java) } == true

fun FieldNode.hasAnnotation(kclass: KClass<*>) =
    visibleAnnotations?.any { it.desc == Type.getDescriptor(kclass.java) } == true
