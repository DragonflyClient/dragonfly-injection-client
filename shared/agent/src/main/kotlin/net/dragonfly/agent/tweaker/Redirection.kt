package net.dragonfly.agent.tweaker

import net.dragonfly.agent.DragonflyAgent
import net.dragonfly.agent.hook.Level
import net.dragonfly.agent.transformer.getAnnotation
import net.dragonfly.agent.transformer.hasAnnotation
import net.dragonfly.obfuscation.Obfuscator
import net.dragonfly.obfuscation.mapping.*
import net.dragonfly.obfuscation.specification.*
import org.objectweb.asm.tree.*

/**
 * `Redirection` manages the redirection step of a [TweakApplier].
 *
 * See the different methods to get to know more about what happens during the redirection
 * process and how the steps are implemented.
 *
 * @param ta The tweak applier that executes the redirection step
 */
internal class Redirection(internal val ta: TweakApplier) {

    /**
     * This list contains the [FieldMapping] of all fields in the source class that have been
     * copied into the destination class and thus are eligible for the redirection process.
     */
    internal val fieldCandidates: List<FieldMapping> = ta.src.fields
        .filter { it.hasAnnotation(Redirect::class) || it.hasAnnotation(Inject::class) }
        .map { field ->
            val targetClass = resolveFieldInHierarchy(field) ?: ta.tweaker.targetClass
            val spec = FieldSpecification(targetClass.className, field.name)

            // field and class are obfuscated (for remapping fields)
            val fieldMapping = Obfuscator.fields().findMapping(spec)
            if (fieldMapping != null) return@map fieldMapping

            // only class is obfuscated (for injecting fields)
            val classMapping = Obfuscator.classes().findMapping(targetClass)
            if (classMapping != null) return@map FieldMapping(classMapping, field.name, field.name)

            // field name remains the same, class name is only changed to target class
            FieldMapping(ClassMapping(targetClass.className, targetClass.className), field.name, field.name)
        }
        .also { list ->
            DragonflyAgent.getInstance().log(
                "> Redirecting fields: \n" + list.joinToString("\n") {
                    "  ${it.deobfuscated} -> ${it.classMapping.obfuscated}.${it.obfuscated}"
                }, Level.TRACE
            )
        }

    /**
     * This list contains the [MethodMapping] of all methods in the source class that have
     * been copied into the destination class and thus are eligible for the redirection process.
     */
    internal val methodCandidates: List<MethodMapping> = ta.src.methods
        .filter { it.hasAnnotation(Redirect::class) || it.hasAnnotation(Inject::class) }
        .map { method ->
            val targetClass = resolveMethodInHierarchy(method) ?: ta.tweaker.targetClass
            val spec = MethodSpecification(targetClass.className, method.name)

            // method and class are obfuscated (for remapping methods)
            val methodMapping = Obfuscator.methods().findMapping(spec)
            if (methodMapping != null) return@map methodMapping

            // only class is obfuscated (for injecting fields)
            val classMapping = Obfuscator.classes().findMapping(targetClass)
            val methodDescriptor = MethodDescriptor.fromString(method.desc)
            if (classMapping != null) {
                return@map MethodMapping(classMapping, method.name, methodDescriptor, method.name, methodDescriptor)
            }

            // method name remains the same, class name is only changed to target class
            MethodMapping(ClassMapping(targetClass.className, targetClass.className), method.name, methodDescriptor, method.name, methodDescriptor)
        }
        .also { list ->
            DragonflyAgent.getInstance().log(
                "> Redirecting methods: \n" + list.joinToString("\n") {
                    "  ${it.deobfuscated} -> ${it.classMapping.obfuscated}.${it.obfuscated}"
                }, Level.TRACE
            )
        }

    /**
     * Finds the class that declares the [field] in the hierarchy of the destination class.
     * The reference is returned by with a [ClassSpecification] or null if the field node
     * has no [Origin] attribute in its [Redirect] annotation.
     */
    private fun resolveFieldInHierarchy(field: FieldNode): ClassSpecification? {
        val origin = parseOriginAttribute(field.getAnnotation(Redirect::class)) ?: return null

        return when (origin[0]) {
            "dynamicOrigin" -> {
                val dynamicOrigin = origin[1] as Array<*>
                val enumValue = dynamicOrigin[1] as String
                when (DynamicOrigin.valueOf(enumValue)) {
                    DynamicOrigin.SUPERCLASS -> ClassSpecification(ta.dest.superName.replace("/", ".")).deobfuscated
                    DynamicOrigin.INTERFACE, DynamicOrigin.NONE -> null
                    // interfaces cannot contain fields
                }
            }
            "staticOrigin" -> ClassSpecification(origin[1] as String)
            else -> null
        }
    }

    /**
     * Finds the class that declares the [method] in the hierarchy of the destination class.
     * The reference is returned by with a [ClassSpecification] or null if the field node
     * has no [Origin] attribute in its [Redirect] annotation.
     */
    private fun resolveMethodInHierarchy(method: MethodNode): ClassSpecification? {
        val origin = parseOriginAttribute(method.getAnnotation(Redirect::class)) ?: return null

        return when (origin[0]) {
            "staticOrigin" -> ClassSpecification(origin[1] as String)
            "dynamicOrigin" -> {
                val dynamicOrigin = origin[1] as Array<*>
                val enumValue = dynamicOrigin[1] as String
                when (DynamicOrigin.valueOf(enumValue)) {
                    DynamicOrigin.SUPERCLASS -> ClassSpecification(ta.dest.superName.replace("/", ".")).deobfuscated
                    DynamicOrigin.INTERFACE -> {
                        ta.dest.interfaces.firstOrNull {
                            Obfuscator.methods().findMapping(
                                MethodSpecification(it.replace("/", "."), method.name, method.desc)
                            ) != null
                        }?.let { ClassSpecification(it.replace("/", ".")) }
                    }
                    DynamicOrigin.NONE -> null
                }
            }
            else -> null
        }
    }

    /**
     * Parses the [Origin] from the [Redirect]-[annotation].
     */
    private fun parseOriginAttribute(annotation: AnnotationNode?): List<Any>? {
        val params = annotation?.values?.takeIf { it.size >= 2 } ?: return null
        val origin = params[1] as? AnnotationNode ?: return null
        return origin.values.takeIf { it.size >= 2 }
    }
}