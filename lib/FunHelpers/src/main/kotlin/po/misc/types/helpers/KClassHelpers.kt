package po.misc.types.helpers

import po.misc.debugging.ClassResolver
import kotlin.reflect.KClass

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
 * Returns the simple class name or `"Anonymous"` if unavailable.
 *
 * Useful for generating human-readable diagnostic messages for
 * anonymous or synthetic types.
 */
val KClass<*>.simpleOrAnon: String get() = simpleName?:"Anonymous"

val KClass<*>.qualifiedOrAnon: String get() = qualifiedName?:"Anonymous"

fun KClass<*>.toKeyParams():KClassParam{
    return KClassParam(simpleOrAnon, qualifiedName?:"N/A", hashCode(), typeParameters.size)
}

val  KClass<out Function<*>>.lambdaName: String  get()  = ClassResolver.classInfo(this).normalizedName