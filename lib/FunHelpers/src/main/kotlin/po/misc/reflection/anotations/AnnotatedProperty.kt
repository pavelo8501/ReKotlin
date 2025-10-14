package po.misc.reflection.anotations

import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberProperties

/**
 * Represents a reflective link between a property of type [T] and an attached annotation [A].
 *
 * An [AnnotatedProperty] provides both the property reference and the annotation instance
 * discovered on that property. It is typically used for reflective inspection,
 * metadata-driven processing, or automatic binding frameworks.
 *
 * Example usage:
 * ```kotlin
 * @MyAnnotation("UserName")
 * var name: String = ""
 *
 * val annotatedProps = collectAnnotated<MyClass, MyAnnotation>()
 * annotatedProps.forEach { (property, annotation) ->
 *     println("${property.name} → ${annotation.value}")
 * }
 * ```
 *
 * @param T The class that declares the annotated property.
 * @param A The annotation type applied to the property.
 * @property property The reflective reference to the annotated property.
 * @property annotation The annotation instance found on the property.
 */
interface AnnotatedProperty<T: Any, A: Annotation>{
    val property: KProperty1<T, *>
    val annotation: A
}

data class AnnotatedPropertyEntry<T: Any, A: Annotation>(
    override val property: KProperty1<T, *>,
    override val annotation: A
): AnnotatedProperty<T, A>



/**
 * Collects all properties of class [T] that are annotated with [A].
 *
 * Returns a list of [AnnotatedProperty] descriptors containing both the property
 * reference and its associated annotation instance.
 *
 * This function is commonly used as a precursor for reflective binding operations,
 * where only annotated properties should be processed (e.g., auto-binding fields
 * in forms or serialization routines).
 *
 * Example:
 * ```kotlin
 * @MyAnnotation
 * var name: String = ""
 *
 * val annotated = collectAnnotated<MyClass, MyAnnotation>()
 * ```
 *
 * @param T The class whose annotated properties should be inspected.
 * @param A The annotation type to look for.
 * @return A list of [AnnotatedProperty] instances, one per annotated property.
 */
inline fun <reified T: Any, reified A : Annotation> collectAnnotated(
): List<AnnotatedProperty<T, A>>{
    val result = mutableListOf<AnnotatedProperty<T, A>>()

    val kClass = T::class
    val annotated = kClass.memberProperties.filter { it.hasAnnotation<A>() }
    val selected = annotated.filterIsInstance<KProperty1<T, Any>>()
    selected.forEach {prop->
        val ann = prop.findAnnotation<A>()
        if (ann != null){
            AnnotatedPropertyEntry(prop, ann)
            result.add(AnnotatedPropertyEntry(prop, ann))
        }
    }
    return result
}


inline fun <T: Any, reified A : Annotation> collectAnnotated(
    typeToken: TypeToken<T>
): List<AnnotatedProperty<T, A>>{
    val result = mutableListOf<AnnotatedProperty<T, A>>()

    val kClass = typeToken.kClass
    val annotated = kClass.memberProperties.filter { it.hasAnnotation<A>() }
    val selected = annotated.filterIsInstance<KProperty1<T, Any>>()
    selected.forEach {prop->
        val ann = prop.findAnnotation<A>()
        if (ann != null){
            AnnotatedPropertyEntry(prop, ann)
            result.add(AnnotatedPropertyEntry(prop, ann))
        }
    }
    return result
}
