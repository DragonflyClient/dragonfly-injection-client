package net.dragonfly.agent.tweaker

import org.objectweb.asm.*

/**
 * This class visitor is responsible for redirecting calls to members of the [tweak applier's][ta]
 * source class to members of the destination class. It provides the [RedirectionMethodVisitor] which
 * transforms the instruction in the class' method bodies.
 *
 * @param redirection A new instance of the [Redirection] that is kept during the whole process
 */
internal class RedirectionClassVisitor(
    private val redirection: Redirection,
) : ClassVisitor(Opcodes.ASM9, redirection.ta.classWriter) {

    private val ta: TweakApplier = redirection.ta

    /**
     * Visits the methods in the class and activates a [RedirectionMethodVisitor] if the given function comes
     * from a tweaker.
     */
    override fun visitMethod(access: Int, name: String, descriptor: String, signature: String?, exceptions: Array<out String>?): MethodVisitor =
        super.visitMethod(access, name, descriptor, signature, exceptions)
            .let { if (ta.isTweakingMethod(name, descriptor)) RedirectionMethodVisitor(redirection, it) else it }
}