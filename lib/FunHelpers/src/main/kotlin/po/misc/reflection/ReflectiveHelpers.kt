package po.misc.reflection

import kotlin.reflect.KProperty
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberProperties


inline fun <reified T: Any, reified A: Annotation> getAnnotated(): List<KProperty<*>> {
    val kClass = T::class
    val annotated = kClass.memberProperties.filter { it.hasAnnotation<A>() }
    return annotated
}