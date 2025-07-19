package po.misc.context

import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.typeOf

/**
 * Represents a structural identity for a given [CTX] type.
 *
 * Combines runtime type information ([KClass] and [KType]) with optional hierarchical context resolution
 * through [parentContext]. This allows building composite identity strings for traceability or matching.
 *
 * @param T The type of the context being identified.
 * @property kClass The runtime class reference of the context.
 * @property kType The full type information including generics.
 * @property parentContext Optional parent context used for hierarchical resolution.
 */
class CTXIdentity<T: CTX> @PublishedApi internal constructor(
    @PublishedApi
    internal val kClass: KClass<T>,
    @PublishedApi
    internal val kType: KType,
    val parentContext: CTX? = null
) {
    internal val name: String = kClass.simpleName ?: "Unnamed"
    /**
     * Hierarchical identity string built from this context and its parents (if any).
     *
     * Format: `Child/Parent/.../Root`
     */
    val completeName: String get() = buildString {
        append(name)
        parentContext?.let {
            append("/")
            append(it.identity.completeName)
        }
    }
    /**
     * Returns [completeName] as the string representation of this identity.
     */
    override fun toString(): String = completeName
}

fun <T, T2>  fromContext(context: T, parentContext:T2? = null): CTXIdentity<T> where T: CTX, T2:CTX {
    val kClass = context::class as KClass<T>
    return parentContext?.let {
        CTXIdentity(kClass, kClass.createType(), it)
    }?:CTXIdentity(kClass, kClass.createType())
}

fun <T>  fromContext(context: T): CTXIdentity<T> where T: CTX{
    val kClass = context::class as KClass<T>
   return CTXIdentity(kClass, kClass.createType())
}

inline fun <reified T>  T.asContext(): CTXIdentity<T> where T: CTX {
    return  CTXIdentity(T::class, typeOf<T>())
}

inline fun <reified T, reified T2>  T.asContext(parentContext: T2): CTXIdentity<T> where T: CTX, T2: CTX {
    require(T::class != T2::class) {
        "Parent context must be of a different class than the current context"
    }
    return  CTXIdentity(T::class, typeOf<T>(), parentContext)
}
