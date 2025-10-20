package po.misc.types.helpers

import kotlin.reflect.KClass
import kotlin.reflect.KType


fun  KType.normalizedName(): String {
    val classifier = classifier as? KClass<*> ?: return "Unknown"
    val typeArgs = arguments.joinToString(", ") { arg ->
        val argType = arg.type
        val argClassifier = argType?.classifier as? KClass<*>
        val argName = argClassifier?.simpleName ?: "Unknown"
        if (argType?.isMarkedNullable == true) "$argName?" else argName
    }
    val baseName = classifier.simpleName ?: "Unknown"
    val nullableSuffix = if (isMarkedNullable) "?" else ""
    return if (arguments.isNotEmpty()) {
        "$baseName<$typeArgs>$nullableSuffix"
    } else {
        "$baseName$nullableSuffix"
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


fun KClass<*>?.simpleNameOr(default: String): String{

    return if(this != null){
        simpleName?:default
    }else{
        default
    }
}