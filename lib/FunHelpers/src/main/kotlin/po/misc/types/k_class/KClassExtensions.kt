package po.misc.types.k_class

import po.misc.debugging.ClassResolver
import kotlin.reflect.KClass

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

fun KClass<*>?.simpleNameOr(default: String): String{
    return if(this != null){
        simpleName?:default
    }else{
        default
    }
}
