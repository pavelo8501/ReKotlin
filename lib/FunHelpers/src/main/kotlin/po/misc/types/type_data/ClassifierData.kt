package po.misc.types.type_data

import po.misc.types.helpers.simpleNameOr
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter

data class TypeParameterData(
    val name: String?,
    val kClass: KClass<*>?,
    val classifier: String,
    val isReified: Boolean = false,
    val upperBounds: List<KClass<*>> = emptyList(),
    val children: List<TypeParameterData> = emptyList()
){
    private val typeParameters : String = children.joinToString {
         it.kClass.simpleNameOr("Unknown")  +  it.classifier
    }
    override fun toString(): String  = name + typeParameters

}

fun KTypeParameter.toTypeData(): TypeParameterData {
    val kClass  = upperBounds.firstOrNull()?.classifier as? KClass<*>
   return TypeParameterData(
        name = name,
        kClass = kClass, // generic params don't map to concrete classes
        classifier = "TypeParameter:$name",
        isReified = isReified,
        upperBounds = upperBounds.mapNotNull { it.classifier as? KClass<*> }
    )
}

fun KType.toTypeParameterData(kClass: KClass<*>): TypeParameterData {
    return when (val classifier = classifier) {
        is KClass<*> -> {
            TypeParameterData(
                name = kClass.simpleNameOr("Unknow"),
                kClass = classifier,
                classifier = classifier.qualifiedName ?: classifier.simpleName ?: "<anonymous>",
                children = arguments.mapNotNull { it.type?.toTypeParameterData(kClass) }
            )
        }
        is KTypeParameter -> {
            classifier.toTypeData()
        }
        else -> TypeParameterData("${kClass.simpleNameOr("Unknow")} <unknown>", null, "<unknown>")
    }
}

fun KType.toTypeParameterData(): TypeParameterData {

    return when (val classifier = classifier) {
        is KClass<*> -> {
            TypeParameterData(
                name = null,
                kClass = classifier,
                classifier = classifier.qualifiedName ?: classifier.simpleName ?: "<anonymous>",
                children = arguments.mapNotNull { it.type?.toTypeParameterData() }
            )
        }
        is KTypeParameter -> classifier.toTypeData()
        else -> TypeParameterData("<unknown>", null, "<unknown>")
    }
}