package po.misc.reflection.properties

import po.misc.exceptions.ManagedException
import po.misc.types.castOrThrow
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.memberProperties

data class PropertyInfo<T: Any, V: Any>(
    val propertyName: String,
    val property: KProperty1<T, V>,
    val returnType: KType
){

}

inline fun <reified T : Any> Any.toPropertyInfo(name: String):PropertyInfo<T, Any>?{
   return  this::class.memberProperties.firstOrNull { it.name == name }?.let { property ->
        property.getter.call()?.let { result ->
            PropertyInfo(property.name, property.castOrThrow<KProperty1<T, Any>, ManagedException>() , result::class.createType())
        }
    }
}

//
//inline fun <reified T : Any> Any.toPropertyInfo():PropertyInfo<T, Any>?{
//
////    return  this::class.objectInstance.t
////        property.getter.call()?.let { result ->
////            PropertyInfo(property.name, property.castOrThrow<KProperty1<T, Any>, ManagedException>() , result::class.createType())
////        }
////    }
//}