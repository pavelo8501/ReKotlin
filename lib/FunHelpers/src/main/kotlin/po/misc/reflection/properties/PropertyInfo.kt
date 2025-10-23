package po.misc.reflection.properties

import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.KVisibility

class StaticTypeToken<T : Any>(
    val value: T,
    val kType: KType,
    val propertyName: String? = null
)

data class PropertyInfo<T: Any, V: Any>(
    val property: KProperty1<T, V>,
    val mutable: Boolean,
    val receiverClass: KClass<T>,
    var valueClass: KClass<V>? = null
){

    val propertyName: String get()= property.name
    val returnType: KType = property.returnType
    val visibility: KVisibility? = property.visibility

    var returnTypeToken : StaticTypeToken<V>? = null

    fun setValueClass(clazz: KClass<V>): PropertyInfo<T, V>{
        valueClass = clazz
        return this
    }

    companion object{
        fun <T: Any, V: Any> create(kProperty1: KProperty1<T, V>, mutable: Boolean,  receiverClass: KClass<T>):PropertyInfo<T, V>{
           return PropertyInfo(kProperty1, mutable, receiverClass)
        }
        fun <T: Any, V: Any> create(kProperty1: KProperty1<T, V>, mutable: Boolean, instance: T):PropertyInfo<T, V>{
            @Suppress("Unchecked_Cast")
            val clazz =  instance::class as KClass<T>
            val property = PropertyInfo(kProperty1, mutable, clazz)
            kProperty1.get(instance).let {
                @Suppress("Unchecked_Cast")
                val resultClass =  it::class as KClass<V>
                property.setValueClass(resultClass)
            }
            return property
        }
    }
}

fun <T : Any> StaticTypeToken<T>.assignIfMatches(map : Map<String, PropertyInfo<*, T>>){
    map.values.forEach {
        if(it.returnType == this.kType){
            it.returnTypeToken = this
        }
    }
}

inline fun <reified T: Any, V: Any> KMutableProperty1<T, V>.toPropertyInfo():PropertyInfo<T, V>{
    val info =  PropertyInfo.create(this, true, T::class)
    return  info
}


fun <T: Any, V: Any> KProperty1<T, V>.toPropertyInfo(clazz: KClass<T>):PropertyInfo<T, V>{
   val info =  PropertyInfo.create(
       kProperty1 = this,
       mutable = false,
       receiverClass = clazz
   )
   return  info
}

fun <T: Any, V: Any> KMutableProperty1<T, V>.toPropertyInfo(clazz: KClass<T>):PropertyInfo<T, V>{
    val info = PropertyInfo(
        property = this,
        mutable = true,
        receiverClass = clazz
    )
    return  info
}
