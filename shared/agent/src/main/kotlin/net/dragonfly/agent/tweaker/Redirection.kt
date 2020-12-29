package net.dragonfly.agent.tweaker

import net.dragonfly.agent.DragonflyAgent
import net.dragonfly.agent.hook.Level
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
            val spec = ta.tweaker.fieldSpecOf(field.name)

            // field and class are obfuscated (for remapping fields)
            val fieldMapping = Obfuscator.fields().findMapping(spec)
            if (fieldMapping != null) return@map FieldMapping(fieldMapping.classMapping, fieldMapping.obfuscated, field.name)

            // only class is obfuscated (for injecting fields)
            val classMapping = Obfuscator.classes().findMapping(ClassSpecification(spec.clazz))
            if (classMapping != null) return@map FieldMapping(classMapping, spec.fieldName, field.name)

            // field name remains the same, class name is only changed to target class
            FieldMapping(ClassMapping(spec.clazz, ta.tweaker.targetClass.className), spec.fieldName, field.name)
        }
        .also { list ->
            DragonflyAgent.getInstance().log(
                "> Redirecting fields: \n" +
                        list.joinToString("\n") { "  ${it.deobfuscated} -> ${it.classMapping.obfuscated}.${it.obfuscated}" },
                Level.TRACE
            )
        }

    /**
     * This list contains the [MethodMapping] of all methods in the source class that have
     * been copied into the destination class and thus are eligible for the redirection process.
     */
    internal val methodCandidates: List<MethodMapping> = ta.src.methods
        .filter { it.hasAnnotation(Redirect::class) || it.hasAnnotation(Inject::class) }
        .map { method ->
            val spec = ta.tweaker.methodSpecOf(method.name, method.desc)
            val sourceDescriptor = MethodDescriptor.fromString(method.desc)
            val translatedDescriptor = MethodDescriptor.fromString(spec.descriptor!!)

            // method and class are obfuscated (for remapping methods)
            val methodMapping = Obfuscator.methods().findMapping(spec)
            if (methodMapping != null) {
                return@map MethodMapping(
                    methodMapping.classMapping,
                    methodMapping.obfuscated, methodMapping.obfuscatedDescriptor,
                    method.name, sourceDescriptor
                )
            }

            // only class is obfuscated (for injecting fields)
            val classMapping = Obfuscator.classes().findMapping(ClassSpecification(spec.clazz))
            if (classMapping != null)
                return@map MethodMapping(
                    classMapping,
                    spec.methodName, translatedDescriptor,
                    method.name, sourceDescriptor
                )

            // method name remains the same, class name is only changed to target class
            MethodMapping(
                ClassMapping(spec.clazz, ta.tweaker.targetClass.className),
                spec.methodName, translatedDescriptor,
                method.name, sourceDescriptor
            )
        }
        .also { list ->
            DragonflyAgent.getInstance().log(
                "> Redirecting methods: \n" +
                        list.joinToString("\n") { "  ${it.deobfuscated} -> ${it.classMapping.obfuscated}.${it.obfuscated}" },
                Level.TRACE
            )
        }
}