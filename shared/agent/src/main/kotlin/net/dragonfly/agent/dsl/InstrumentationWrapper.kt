package net.dragonfly.agent.dsl

import javassist.*
import net.dragonfly.agent.DragonflyAgent
import net.dragonfly.agent.transformer.*
import net.dragonfly.agent.tweaker.*
import net.dragonfly.obfuscation.specification.ClassSpecification
import java.lang.instrument.ClassFileTransformer
import java.lang.instrument.Instrumentation

class InstrumentationWrapper(
    private val instrumentation: Instrumentation,
    private val dragonflyAgent: DragonflyAgent,
) {
    fun editClass(className: String, editFunction: CtClass.() -> Unit) {
        instrumentation.addTransformer(EditTransformer(ClassSpecification(className), editFunction), true)
        DragonflyAgent.getInstance().log("> Registered edit transformer for $className")
    }

    fun tweaker(tweaker: Tweaker) {
        instrumentation.addTransformer(TweakTransformer(tweaker), true)
        DragonflyAgent.getInstance().log("> Registered tweak transformer for ${tweaker.targetClass}")
    }

    fun transformer(transformer: ClassFileTransformer) {
        instrumentation.addTransformer(transformer)
        DragonflyAgent.getInstance().log("> Registered transformer ${transformer::class.simpleName}")
    }
}