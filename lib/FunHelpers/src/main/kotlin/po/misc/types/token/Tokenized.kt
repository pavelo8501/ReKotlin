package po.misc.types.token

import po.misc.debugging.ClassResolver
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf


interface TypeProvider{
    val types: List<TypeToken<*>>

    val typeName:String get() = types.joinToString(prefix = "<", separator = ",", postfix = ">") {
        it.typeName
    }
}

interface TokenHolder: TypeProvider{
    val typeToken: TypeToken<*>
    override val types: List<TypeToken<*>> get() = listOf(typeToken)
}

/**
 * Marks a component as being associated with a concrete [TypeToken].
 *
 * A [Tokenized] element exposes its primary type information via [typeToken].
 * This type is typically used by render plans, nodes, or resolvers to determine
 * compatibility with receivers at runtime.
 *
 * @param T the primary receiver type represented by this tokenized element
 */
interface Tokenized<T> : TokenHolder, TokenFactory{
    override val typeToken: TypeToken<T>
}

/**
 * A [Tokenized] component capable of resolving a value of type [V]
 * from a receiver of type [T].
 *
 * This interface represents a two-sided type contract:
 *
 * - [receiverType] — the type this resolver can accept as an input (receiver)
 * - [valueType] — the type this resolver produces or exposes as a value
 *
 * It is commonly used by render plans and nodes to determine whether a
 * particular element can participate in a resolution or rendering chain.
 *
 * @param T the receiver type this resolver operates on
 * @param V the resolved value type produced from the receiver
 */
interface TokenizedResolver<T, V> : Tokenized<V>{
    val sourceType: TypeToken<T>
    val receiverType: TypeToken<V>

    override val typeToken: TypeToken<V> get() = receiverType
    override val types: List<TypeToken<*>> get() = listOf(sourceType, receiverType)

    /**
     * Determines whether this resolver can accept the given receiver class.
     *
     * Collection and nullable receiver types are resolved using
     * effective (runtime) type semantics.
     */
    fun acceptsReceiver(receiverClass: KClass<*>):Boolean{
        return receiverType.effectiveClassIs(receiverClass)
    }
    fun acceptsReceiver(kType: KType):Boolean{
        return receiverType.kType == kType
    }
    /**
     * Determines whether this resolver can produce or expose a value
     * of the given class.
     *
     * Collection and nullable value types are resolved using
     * effective (runtime) type semantics.
     */
    fun resolvesValue(receiverClass: KClass<*>):Boolean{
        return sourceType.effectiveClassIs(receiverClass)
    }
    fun resolvesValue(kType: KType):Boolean{
        return receiverType.kType == kType
    }

}

/**
 * Checks whether this resolver accepts a receiver of the reified type [T].
 * This is a type-safe, DSL-friendly alternative to [TokenizedResolver.acceptsReceiver].
 */
inline fun <reified T> TokenizedResolver<*, *>.acceptsReceiverOf(): Boolean =
    acceptsReceiver(typeOf<T>())

/**
 * Checks whether this resolver resolves a value of the reified type [V].
 * This is a type-safe, DSL-friendly alternative to [TokenizedResolver.resolvesValue].
 */
inline fun <reified V> TokenizedResolver<*, *>.resolvesValueOf(): Boolean =
    resolvesValue(typeOf<V>())


