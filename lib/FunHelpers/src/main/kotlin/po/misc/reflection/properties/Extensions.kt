package po.misc.reflection.properties

import po.misc.data.anotation.Composable
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberProperties


inline fun <T: Any, reified A: Annotation>  takePropertySnapshot(obj: T): Map<String, Any?>{
    val snapshot: MutableMap<String, Any?> = mutableMapOf()
    val kClass = obj::class
    for(property in  kClass.memberProperties) {
        if (property.findAnnotations<A>().isNotEmpty()) {
            val value = property.getter.call(obj)
            snapshot[property.name] = value
        }
    }
    return snapshot.toMap()
}


fun KClass<*>.findAllAnnotated(): List<KProperty<*>> {
  val result =  memberProperties.filter {
        it.hasAnnotation<Composable>()
    } + memberProperties.mapNotNull { (it.returnType.classifier as? KClass<*>)?.findAllAnnotated() }.flatten()
   return result
}
