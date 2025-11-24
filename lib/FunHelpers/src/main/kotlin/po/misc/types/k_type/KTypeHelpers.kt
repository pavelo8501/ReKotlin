package po.misc.types.k_type

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



