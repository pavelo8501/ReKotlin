package po.misc.reflection.properties

import po.misc.data.anotation.Composable
import po.misc.data.printable.knowntypes.PropertyData
import po.misc.context.CTX
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberProperties


inline fun <T: CTX, reified A: Annotation>  takePropertySnapshot(obj: T): List<PropertyData>{
    val propertySnapshot: MutableList<PropertyData> = mutableListOf()
    val kClass = obj::class
    for(property in  kClass.memberProperties) {

        if (property.findAnnotations<A>().isNotEmpty()) {
            try {
                val value = property.getter.call(obj)
                propertySnapshot.add(PropertyData(obj, property.name, value.toString()))
            }catch (ex: Throwable){

            }
        }
    }
    return propertySnapshot.toList()
}






