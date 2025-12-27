package po.misc.types.k_class


import po.misc.debugging.ClassResolver
import po.misc.exceptions.throwableToText
import po.misc.types.ClassAware
import po.misc.types.ReflectiveLookup
import po.misc.types.safeCast
import po.misc.types.token.GenericInfo
import po.misc.types.token.TypeSlot
import po.misc.types.token.TypeToken
import po.misc.types.token.asTAndAny
import java.beans.Visibility
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.KVisibility
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.typeOf


@PublishedApi
internal fun <T>  KClass<*>.asDefinitelyNotNull(): KClass<T & Any>{
    @Suppress("UNCHECKED_CAST")
    val kClass = this as? KClass<T & Any>
    if (kClass != null){
        return kClass
    }else{
        throw IllegalArgumentException("${this.qualifiedName} cannot be cast to <T & Any>")
    }
}

@PublishedApi
internal fun <T> KClass<*>.checkDefinitelyNotNull(): KClass<T & Any>?{
    try {
        @Suppress("UNCHECKED_CAST")
        return this as? KClass<T & Any>
    }catch (e: Throwable){
        return null
    }
}


/**
 * Computes a **linear class hierarchy chain** for this [KClass].
 *
 * Unlike a full recursive supertype traversal, this function intentionally follows
 * **only the first declared supertype** at each step.
 *
 * This produces a *single inheritance chain*, for example:
 *
 * ```
 * LogMessage
 *   → StructuredBase
 *     → StructuredLoggable
 *       → Loggable
 *         → Printable
 *           → Any
 * ```
 *
 * ### Behaviour
 * - The hierarchy is **linear**, not a graph: only the *first* supertype is used.
 * - Up to [maxDepth] entries are collected.
 * - Traversal stops early if:
 *   - `stopBefore` is reached (default: `Any::class`), or
 *   - a class repeats (cycle protection).
 * - The starting class (`this`) **is included** as the first element.
 *
 * @param maxDepth Maximum number of hierarchy steps to follow
 * @param stopBefore A class at which traversal will stop *before* adding it
 *
 * @return A list of classes in linear hierarchy order, starting from this class.
 */
fun KClass<*>.computeHierarchy(
    maxDepth: Int,
    stopBefore: KClass<*> = Any::class
): List<KClass<*>> {
    val result = mutableListOf<KClass<*>>()
    val visited = mutableSetOf<KClass<*>>()
    var current: KClass<*>? = this
    repeat(maxDepth) {
        current = current
            ?.takeUnless { it == stopBefore }
            ?.takeIf { visited.add(it) }
            ?.let { klass ->
                result += klass
                klass.supertypes.firstNotNullOfOrNull { it.classifier as? KClass<*> }
            }
        if (current == null || current == stopBefore) return result
    }
    return result
}


fun <T: Any>  KClass<*>.readAllProperties(receiver: T): List<String>{
    val result = mutableListOf<String>()

    this.memberProperties.forEach {prop->
        try {
            prop.safeCast<KProperty1<T, Any>>()?.let {casted->
                casted.visibility?.let {
                   if(it == KVisibility.PUBLIC){
                       result.add("${casted.name}: ${casted.get(receiver)}")
                   }
                }
            }
        }catch (e: Throwable){
            result.add( "readAllProperties -> ${e.throwableToText()}")
        }
    }
    return result
}

class KClassAware<T> @PublishedApi internal constructor(override val kClass: KClass<T & Any>): ClassAware<T>

@PublishedApi
internal val <T: Any> KClass<T>.asClasAware : KClassAware<T> get() = KClassAware(this)


@PublishedApi
internal inline fun <reified T> clasAware(): ClassAware<T>{
   return KClassAware(T::class.asTAndAny())
}



