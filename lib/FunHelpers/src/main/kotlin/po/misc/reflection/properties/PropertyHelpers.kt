package po.misc.reflection.properties

import po.misc.reflection.mappers.models.PropertyRecord
import kotlin.reflect.KProperty
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.typeOf


inline fun <reified T: Any> toPropertyMap(): Map<String, PropertyRecord<T>>
    = T::class.memberProperties.associate {it.name to PropertyRecord.create(it)}



@JvmName("toPropertyMapWithFilter")
inline fun <reified T : Any, reified F : Any> toPropertyMap(): Map<String, PropertyRecord<T>>{
    val properties = T::class.memberProperties
    val filtered = properties.filter { it.returnType.isSubtypeOf(typeOf<F>()) }
    return filtered.associate { it.name to PropertyRecord.create(it) }
}

fun <V: Any> KProperty<V>.toRecord(): PropertyRecord<V>{
   return PropertyRecord.create(this)
}


