package po.misc.reflection.properties

import po.misc.reflection.mappers.models.PropertyRecord
import po.misc.types.safeCast
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.typeOf


inline fun <reified T: Any> toPropertyMap(): Map<String, PropertyRecord<T>>
    = T::class.memberProperties.associate {it.name to PropertyRecord.create(it)}


@JvmName("toPropertyMapWithFilter")
inline fun <reified T : Any, reified F : Any> toFilteredPropertyMap(): Map<String, PropertyRecord<T>>{
    val properties = T::class.memberProperties
    val filtered = properties.filter { it.returnType.isSubtypeOf(typeOf<F>()) }
    return filtered.associate { it.name to PropertyRecord.create(it) }
}

fun <T: Any> toPropertyMap(clazz: KClass<T>):Map<String, PropertyInfo<T, Any>> {
    val propertyInfoList = clazz.memberProperties.mapNotNull { it.safeCast<KProperty1<T, Any>>()?.toPropertyInfo(clazz)}
    return  propertyInfoList.associateBy{ it.propertyName }
}


fun <V: Any> KProperty<V>.toRecord(): PropertyRecord<V>{
   return PropertyRecord.create(this)
}

@JvmName("findPropertiesOfTypeStrictTyped")
inline fun <T: Any, reified P : Any> KClass<T>.findPropertiesOfType(): List<KProperty1<T, P>> {
    @Suppress("UNCHECKED_CAST")
    return this.memberProperties
        .filter { filteredProp->
            filteredProp.returnType.jvmErasure == P::class
        }
        .mapNotNull {mapped->
            mapped  as? KProperty1<T, P>
        }
}


inline fun <reified P : Any> KClass<*>.findPropertiesOfType(): List<KProperty1<Any, P>> {
    @Suppress("UNCHECKED_CAST")
    return this.memberProperties
        .filter { filteredProp->
            filteredProp.returnType.jvmErasure == P::class
        }
        .mapNotNull {mapped->
            mapped  as? KProperty1<Any, P>
        }
}






