package net.dragonfly.agent.tweaker

import net.dragonfly.agent.DragonflyAgent
import net.dragonfly.agent.hook.Level
import net.dragonfly.agent.transformer.hasAnnotation
import net.dragonfly.obfuscation.specification.*
import org.objectweb.asm.*
import org.objectweb.asm.tree.ClassNode
import java.io.File

/**
 * This class performs the steps to apply a tweaker to its target class.
 *
 * @param tweaker The tweaker which is applied
 * @param src The bytecode-level representation of the tweaker class
 * @param dest The bytecode-level representation of the tweaker's target class
 */
internal class TweakApplier private constructor(
    internal val tweaker: Tweaker,
    internal val src: ClassNode,
    internal val dest: ClassNode,
) {
    companion object {
        /**
         * Creates a new instance with the given parameters of the function.
         */
        internal fun create(tweaker: Tweaker, from: ClassNode, into: ClassNode) = TweakApplier(tweaker, from, into)
    }

    /**
     * The class writer which will finalize the tweaking process by constructing
     * the class based on the bytecode instructions.
     */
    internal val classWriter = ClassWriter(ClassWriter.COMPUTE_FRAMES)

    /**
     * Returns the written class in the form of a byte array.
     */
    fun writeClass(): ByteArray = classWriter.toByteArray()

    /**
     * Writes the written class to a file in the "classes" folder of the current working directory if the [target]
     * is null or if it is contained in the name of the tweaker class.
     *
     * This function is made to be invoked between other method invocations on this instance so it will catch and
     * ignore all exceptions to prevent the transformation flow from being interrupted.
     */
    fun debug(target: String? = null) = apply {
        try {
            if (target == null || src.name.contains(target)) {
                val outputFile = File("classes\\" + dest.name.replace("/", ".") + ".class")
                outputFile.writeBytes(classWriter.toByteArray())
            }
        } catch (ignored: Throwable) {
        }
    }

    /**
     * "Substitutes" the corresponding methods in the destination class with the methods from the source
     * (tweaker) class annotated with [@Substitute][Substitute].
     *
     * This means that the full bytecode of the method (including its body, parameters, annotations) is copied
     * into the destination class and the old method is removed from it. The name of the target method as well
     * as the name of the target class are calculated in this step by looking for obfuscation mappings. If the
     * method to be substituted is not present in the target class, the substitution is skipped and an
     * error message is printed to the game output.
     */
    fun runMethodSubstitution() = apply {
        val candidates = src.methods.filter { it.hasAnnotation(Substitute::class) }
        candidates.forEach { method ->
            val spec = tweaker.methodSpecOf(method.name, method.desc).obfuscated
            val old = dest.methods.firstOrNull { o -> o.name == spec.methodName && o.desc == spec.descriptor }

            if (old != null) {
                DragonflyAgent.getInstance().log("> Substituting method ${old.name}${old.desc} with ${method.name}", Level.TRACE)
                method.access = old.access
                dest.methods.remove(old)
            } else {
                DragonflyAgent.getInstance().log("> Method ${method.name} not present in class ${dest.name}", Level.ERROR)
                return@forEach
            }

            method.name = spec.methodName
            method.desc = spec.descriptor
            dest.methods.add(method)
        }
    }

    /**
     * Injects the methods from the source class annotated with [@Inject][Inject] into the destination class.
     *
     * The full method bytecode including the body, annotations and parameters is copied.
     */
    fun runMethodInjection() = apply {
        val candidates = src.methods.filter { it.hasAnnotation(Inject::class) }
        candidates.forEach { method ->
            DragonflyAgent.getInstance().log("> Injecting method ${method.name}", Level.TRACE)
            dest.methods.add(method)
        }
    }

    /**
     * Injects the fields from the source class annotated with [@Inject][Inject] into the destination class.
     *
     * Additionally, access to the fields from the original bytecode is redirected to the copied fields
     * in the [redirection step][runRedirection] since the target class cannot access members of the tweaker
     * class.
     */
    fun runFieldInjection() = apply {
        val candidates = src.fields.filter { it.hasAnnotation(Inject::class) }
        candidates.forEach { field ->
            DragonflyAgent.getInstance().log("> Injecting field ${field.name}", Level.TRACE)
            dest.fields.add(field)
        }
    }

    /**
     * Redirects accesses to a member of the source class to a member of the destination class.
     *
     * More specifically, this step searches the bytecode instructions in the methods of the destination
     * class for accesses to a member of the source class and changes them to access the corresponding
     * member in the destination class instead.
     *
     * - For source members annotated with [@Inject][Inject], the new injected member in the destination class is used.
     * - For source members annotated with [@Redirect][Redirect], the target member in the destination class is used.
     *
     * This step also handles the obfuscation of classes and of the members (only for [@Redirect][Redirect]).
     */
    fun runRedirection() = apply {
        val redirection = Redirection(this)
        val cv = RedirectionClassVisitor(redirection)
        dest.accept(cv)
    }

    /**
     * Returns whether the method (specified by its [name] and [descriptor]) is a tweaking, i.e. is annotated
     * with either [@Inject][Inject] or [@Substitute][Substitute].
     */
    fun isTweakingMethod(name: String, descriptor: String) =
        src.methods.filter { it.hasAnnotation(Inject::class) || it.hasAnnotation(Substitute::class) }
            .any { it.name == name && it.desc == descriptor }
}