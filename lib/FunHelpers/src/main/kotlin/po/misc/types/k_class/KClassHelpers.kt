package po.misc.types.k_class


import po.misc.debugging.ClassResolver
import po.misc.types.token.GenericInfo
import po.misc.types.token.TypeSlot
import kotlin.reflect.KClass
import kotlin.reflect.KTypeParameter
import kotlin.reflect.typeOf

data class KClassParam(
    val simpleName : String,
    val qualifiedName: String,
    val hashCode: Int,
    val typeParameters: Int
){
    override fun toString(): String {
        return buildString {
            appendLine("Simple Name: $simpleName")
            appendLine("Qualified Name: $qualifiedName")
            appendLine("Hash Code: $hashCode")
            appendLine("Type Parameters Count: $typeParameters")
        }
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


@PublishedApi
internal inline fun <reified T: Any> KClass<T>.typeSlots():List<TypeSlot>{

    val result = mutableListOf<TypeSlot>()
    val kType = typeOf<T>()
    val typeParameters = T::class.typeParameters
    kType.arguments.forEachIndexed { index, arg ->
        val parameter =  typeParameters.getOrNull(index)?: return@forEachIndexed
        arg.type?.let { argType ->
            (argType.classifier as? KClass<*>)?.let { klass ->
                val genericParam = GenericInfo(parameter.name, argType,  ClassResolver.classInfo(klass))
                val typeSlot = TypeSlot(genericParam, parameter)
                result.add(typeSlot)
            }
        }
    }
    return result
}