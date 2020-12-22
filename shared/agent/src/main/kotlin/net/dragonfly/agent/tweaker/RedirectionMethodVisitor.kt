package net.dragonfly.agent.tweaker

import org.objectweb.asm.*

/**
 * This method visitor transforms methods that come from a tweaker and redirects calls to members
 * of the tweaker class to the target class.
 *
 * @param redirection The redirection configuration that has been computed by the tweak applier
 * @param mv The next method visitor in the visiting chain
 */
internal class RedirectionMethodVisitor(private val redirection: Redirection, mv: MethodVisitor?) : MethodVisitor(Opcodes.ASM9, mv) {

    private val ta: TweakApplier = redirection.ta

    /**
     * Replaces the read/write accesses to fields from source class class with read/write accesses
     * to the destination class.
     */
    override fun visitFieldInsn(opcode: Int, owner: String?, name: String?, descriptor: String?) {
        // note that this COULD cause bugs since all calls to this.<superclass member> are
        // replaced with super.<superclass member>
        val targetField = redirection.fieldCandidates.firstOrNull {
            it.deobfuscated == name && ta.src.name == owner
        } ?: return super.visitFieldInsn(opcode, owner, name, descriptor)

        return super.visitFieldInsn(opcode, targetField.classMapping.obfuscated, targetField.obfuscated, descriptor)
    }

    /**
     * In order to access the fields in a class, an instance of `this` is pushed to the operand stack.
     * For methods originally coming from the source class, the type of `this` is their source class.
     * This method rewrites the destination classes and changes all occurrences of local variables
     * with the type of the source class to have the type of the destination class.
     */
    override fun visitLocalVariable(name: String?, descriptor: String?, signature: String?, start: Label?, end: Label?, index: Int) {
        if (descriptor == Type.getDescriptor(ta.tweaker::class.java)) {
            return super.visitLocalVariable(name, "L${ta.dest.name};", signature, start, end, index)
        }

        super.visitLocalVariable(name, descriptor, signature, start, end, index)
    }

    /**
     * Redirect invocations of methods in the source class to call the corresponding methods in the
     * destination class. The function name, owner and descriptor are obfuscated during this redirection.
     */
    override fun visitMethodInsn(opcode: Int, owner: String, name: String, descriptor: String, isInterface: Boolean) {
        val targetMethod = redirection.methodCandidates.firstOrNull {
            it.deobfuscated == name && it.deobfuscatedDescriptor.toString() == descriptor && ta.src.name == owner
        } ?: return super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)

        return super.visitMethodInsn(opcode, targetMethod.classMapping.obfuscated, targetMethod.obfuscated, targetMethod.obfuscatedDescriptor.toString(), isInterface)
    }
}