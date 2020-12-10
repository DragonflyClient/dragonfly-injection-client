package net.dragonfly.agent.dsl

import javassist.*
import net.dragonfly.agent.Agent
import net.dragonfly.agent.log
import net.dragonfly.agent.transformer.*
import net.dragonfly.agent.tweaker.*
import net.dragonfly.obfuscation.specification.ClassSpecification
import java.lang.instrument.ClassFileTransformer
import java.lang.instrument.Instrumentation

class InstrumentationWrapper(
    private val instrumentation: Instrumentation,
    private val agent: Agent,
) {
    fun editClass(className: String, editFunction: CtClass.() -> Unit) {
        instrumentation.addTransformer(EditTransformer(ClassSpecification(className), editFunction), true)
        log("> Registered edit transformer for $className")
    }

    fun tweaker(tweaker: Tweaker) {
        instrumentation.addTransformer(TweakTransformer(tweaker), true)
        log("> Registered tweak transformer for ${tweaker.targetClass}")
    }

    fun transformer(transformer: ClassFileTransformer) {
        instrumentation.addTransformer(transformer)
        log("> Registered transformer ${transformer::class.simpleName}")
    }
}