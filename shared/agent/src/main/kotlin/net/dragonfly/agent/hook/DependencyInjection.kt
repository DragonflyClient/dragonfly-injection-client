package net.dragonfly.agent.hook

import org.koin.core.context.GlobalContext
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier
import org.koin.core.component.KoinComponent

/**
 * Allows direct access to the `get` function of Koin from outside a [KoinComponent].
 *
 * @see org.koin.core.component.get
 */
inline fun <reified T : Any> get(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null
): T =
    getKoin().get(qualifier, parameters)

/**
 * Allows direct access to the `inject` delegate of Koin from outside a [KoinComponent].
 *
 * @see org.koin.core.component.inject
 */
inline fun <reified T : Any> inject(
    qualifier: Qualifier? = null,
    mode: LazyThreadSafetyMode = LazyThreadSafetyMode.SYNCHRONIZED,
    noinline parameters: ParametersDefinition? = null
): Lazy<T> =
    lazy(mode) { getKoin().get<T>(qualifier, parameters) }

/**
 * Allows direct access to the `bind` function of Koin from outside a [KoinComponent].
 *
 * @see org.koin.core.component.bind
 */
inline fun <reified S : Any, reified P : Any> bind(
    noinline parameters: ParametersDefinition? = null
): S =
    getKoin().bind<S, P>(parameters)

/**
 * Returns Koin from the global context.
 */
fun getKoin() = GlobalContext.get()