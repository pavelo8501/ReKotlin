package po.misc.types.token

import po.misc.context.tracable.TraceableContext
import po.misc.types.token.TypeToken.CreateOptions
import kotlin.reflect.KClass

interface TokenFactory

/**
 * Creates a [TypeToken] for the reified generic type [T].
 *
 * This is the primary helper for generating tokens without needing
 * to specify any base class information. Useful when working with
 * simple, non-polymorphic types.
 *
 * @return a new [TypeToken] representing type [T].
 */
inline fun <reified T> TokenFactory.tokenOf(options:  CreateOptions? = null): TypeToken<T>{
    return TypeToken.create<T>(options)
}

/**
 * Creates a [TypeToken] for a generic type [T], using [GT] as the concrete
 * runtime subtype and [baseClass] as the declared base type.
 *
 * Use this overload when you want to explicitly control the base type that
 * the token represents—typically in polymorphic or reflection-heavy scenarios.
 *
 * Example:
 * ```
 * val token = TokenFactory.tokenOf<BaseClass, DerivedClass>(BaseClass::class)
 * ```
 *
 * @param baseClass the declared upper type that the resulting token should represent.
 * @return a new [TypeToken] representing [T], backed by concrete type [GT].
 */
inline fun <T, reified GT: T> TokenFactory.tokenOf(baseClass: KClass<T & Any>, options:  CreateOptions? = null): TypeToken<T>{
  return TypeToken.create<T, GT>(baseClass, options)
}

/**
 * Creates a [TypeToken] for a [TraceableContext] implementation [T].
 *
 * This is a convenience extension allowing any [TraceableContext] instance
 * to quickly produce its own type token without needing to reference [TokenFactory].
 *
 * @receiver the current instance of a [TraceableContext] implementation.
 * @return a new [TypeToken] representing type [T].
 */
inline fun <reified T: TraceableContext> T.toToken(): TypeToken<T>{
    return TypeToken.create<T>()
}

/**
 * Creates a [TypeToken] for a [TraceableContext] base type [T], using the
 * receiver [GT] as the concrete runtime subtype and [baseClass] as the declared base type.
 *
 * Useful in more advanced scenarios where a context instance must report itself
 * under a parent type, for example when working with tracing, logging, or DI systems
 * requiring polymorphic handling.
 *
 * @receiver the concrete context instance of subtype [GT].
 * @param baseClass the declared base type for the generated token.
 * @return a new [TypeToken] representing [T], backed by concrete type [GT].
 */
inline fun <reified T: TraceableContext, reified GT: T> GT.toToken(baseClass: KClass<T>): TypeToken<T>{
    return TypeToken.create<T, GT>(baseClass)
}

