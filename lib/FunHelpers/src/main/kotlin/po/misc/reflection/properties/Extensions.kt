package po.misc.reflection.properties

import po.misc.interfaces.IdentifiableContext
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.full.memberProperties


inline fun <T: Any, reified A: Annotation>  IdentifiableContext.takePropertySnapshot(obj: T): Map<String, Any?>{
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