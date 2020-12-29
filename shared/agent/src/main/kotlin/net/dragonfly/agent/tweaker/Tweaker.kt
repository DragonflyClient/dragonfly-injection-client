package net.dragonfly.agent.tweaker

import com.fasterxml.jackson.databind.node.ObjectNode
import net.dragonfly.agent.DragonflyAgent
import net.dragonfly.agent.util.asObject
import net.dragonfly.agent.util.mergeJson
import net.dragonfly.agent.dsl.InstrumentationWrapper
import net.dragonfly.agent.hook.InjectionHook
import net.dragonfly.agent.hook.Level
import net.dragonfly.obfuscation.specification.*
import java.lang.IllegalStateException
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.annotation.AnnotationTarget.FUNCTION

/**
 * The superclass of any tweaker class.
 *
 * A tweaker can be registered using the [InstrumentationWrapper.tweaker] function in the
 * injection hook's [configuration block][InjectionHook.configure].
 *
 * @param inputTargetClassName The name of the class that is tweaked by this tweaker. Note that
 * this parameter has a lower priority than the `target` property of the [tweaker configuration]
 * [config].
 */
open class Tweaker(private val inputTargetClassName: String?) {

    /**
     * Instantiates a new tweaker with `null` as the parameter for the target class. This
     * implies that the target class **must** be set by the [tweaker configuration][config].
     */
    constructor() : this(inputTargetClassName = null)

    /**
     * The tweaker configuration represented by a JSON object. The config is loaded lazily
     * and can be assembled by multiple configuration files in different injection hooks.
     *
     * Note that the config is **not** deeply immutable which means that its properties can
     * be modified by any client that can access this field.
     *
     * @see loadConfiguration
     */
    val config: ObjectNode? by lazy { loadConfiguration() }

    /**
     * The qualified name of the class that is tweaked by the tweaker, called it's "target class".
     *
     * This property is computed lazily with the configuration's `target` property having the
     * highest priority. If the configuration doesn't specify a `target` property, the tweaker
     * falls back to the [inputTargetClassName] constructor parameter.
     *
     * If the parameter is also null, the computation will fail and throw an [IllegalStateException]
     * which will result in the transformation process to fail.
     */
    val targetClass by lazy {
        ClassSpecification(
            config?.get("target")?.textValue()
                ?: inputTargetClassName
                ?: throw IllegalStateException("${this::class.qualifiedName} has no target class")
        )
    }

    /**
     * Creates a [MethodSpecification] matching a method of the tweaker (source class) that is
     * defined by the [name] and [descriptor]. Additionally, this function looks inside the
     * [config] for translations of this method and applies them to the returned [MethodSpecification].
     */
    fun methodSpecOf(name: String, descriptor: String): MethodSpecification {
        val translation = lookupMethodTranslation(qualifier = name + descriptor)

        return MethodSpecification(
            clazz = translation?.owner ?: targetClass.className,
            methodName = translation?.name ?: name,
            descriptor = translation?.descriptor ?: descriptor
        )
    }

    /**
     * Creates a [FieldSpecification] matching a field of the tweaker (source class) that is
     * defined by the [name]. Additionally, this function looks inside the [config] for
     * translations of this field and applies them to the returned [FieldSpecification].
     */
    fun fieldSpecOf(name: String): FieldSpecification {
        val translation = lookupFieldTranslation(name)

        return FieldSpecification(
            clazz = translation?.owner ?: targetClass.className,
            fieldName = translation?.name ?: name
        )
    }

    /**
     * Loads the configuration of the tweaker by searching the classpath of every injection
     * hook for files matching the name of the tweaker class and a *.json* extension.
     *
     * The configuration which is found first will act as the base configuration, while all
     * other configs found later are merged into the base one using the [mergeJson] function.
     * Since the injection hooks are ordered according to their priority, properties of the
     * configuration can be overwritten as required by the injection hook system.
     *
     * If not a single config file is found, this function will return null which results in
     * the [tweaker config][config] being null.
     *
     * If an error occurs during the parsing of a config file, the file is skipped and the
     * process can continue. This is important since
     */
    private fun loadConfiguration(): ObjectNode? {
        var result: ObjectNode? = null

        DragonflyAgent.getInstance().injectionHooks.forEach { hook ->
            val resourceName = "/${hook.simpleName}/${this::class.java.name}.json"
            hook::class.java.getResourceAsStream(resourceName)?.runCatching {
                val content = bufferedReader().readText()
                val parsed = DragonflyAgent.getInstance().jackson.readTree(content) as ObjectNode

                result = if (result == null) parsed else mergeJson(result!!, parsed)
            }?.onFailure { throwable ->
                DragonflyAgent.getInstance().log("Exception during parsing of tweaker config file $resourceName " +
                        "in classpath of injection hook ${hook.qualifiedName}", Level.ERROR)
                DragonflyAgent.getInstance().log(throwable, Level.ERROR)
            }
        }

        return result
    }

    /**
     * Searches the [config] for a translation of the method specified by the [qualifier]
     * inside the `methods` object of the [config].
     *
     * A translation can either be defined by a simple string-to-string mapping which translates
     * only the name of the method, or a string-to-object mapping in which the object can define
     * a translation for the `name`, `descriptor` and `owner` using the same-called properties
     * respectively.
     *
     * The function returns an instance of [MethodTranslation] on which the parts of the method
     * that should be translated are given, while others are kept to null. See [MethodTranslation]
     * for more information.
     */
    private fun lookupMethodTranslation(qualifier: String): MethodTranslation? =
        config?.get("methods")?.asObject()?.get(qualifier)?.let {
            if (it.isTextual) {
                MethodTranslation(name = it.textValue())
            } else {
                val obj = it.asObject()
                MethodTranslation(
                    name = obj.get("name")?.textValue(),
                    descriptor = obj.get("descriptor")?.textValue(),
                    owner = obj.get("owner")?.textValue()
                )
            }
        }

    /**
     * Searches the [config] for a translation of the field specified by the [name]
     * inside the `fields` object of the [config].
     *
     * A translation can either be defined by a simple string-to-string mapping which translates
     * only the name of the field, or a string-to-object mapping in which the object can define
     * a translation for the `name` and `owner` using the same-called properties respectively.
     *
     * The function returns an instance of [FieldTranslation] on which the parts of the field
     * that should be translated are given, while others are kept to null. See [FieldTranslation]
     * for more information.
     */
    private fun lookupFieldTranslation(name: String): FieldTranslation? =
        config?.get("fields")?.asObject()?.get(name)?.let {
            if (it.isTextual) {
                FieldTranslation(name = it.textValue())
            } else {
                val obj = it.asObject()
                FieldTranslation(
                    name = obj.get("name")?.textValue(),
                    owner = obj.get("owner")?.textValue()
                )
            }
        }
}

/**
 * Specifies that the annotated method should replace ("substitute") the original method in the
 * target class.
 */
@Target(FUNCTION)
annotation class Substitute

/**
 * Specifies that the annotated method or field should be added ("injected") into the target class.
 */
@Target(FUNCTION, FIELD)
annotation class Inject

/**
 * Specifies that calls to the annotated method or field should be replaced with calls to the
 * corresponding member in the target class ("redirected").
 */
@Target(FUNCTION, FIELD)
annotation class Redirect

/**
 * Simple container class to hold the data returned from [Tweaker.lookupMethodTranslation].
 */
private data class MethodTranslation(val name: String? = null, val descriptor: String? = null, val owner: String? = null)

/**
 * Simple container class to hold the data returned from [Tweaker.lookupFieldTranslation].
 */
private data class FieldTranslation(val name: String? = null, val owner: String? = null)