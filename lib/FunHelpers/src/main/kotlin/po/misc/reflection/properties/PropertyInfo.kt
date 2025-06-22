package po.misc.reflection.properties


import po.misc.collections.StaticTypeKey
import po.misc.data.helpers.textIfNull
import po.misc.types.toSimpleNormalizedKey
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.KVisibility
import kotlin.reflect.typeOf

class StaticTypeToken<T : Any>(
    val value: T,
    val kType: KType,
    val propertyName: String? = null
)

data class PropertyInfo<T: Any, V: Any>(
    val property: KProperty1<T, V>,
    internal val clazz: KClass<T>,
){
    val typeKey = StaticTypeKey(clazz)
    val propertyName: String get()= property.name
    val returnType: KType = property.returnType
    val visibility: KVisibility? = property.visibility


    var returnTypeKey: StaticTypeKey<V>? = null

    var returnTypeToken : StaticTypeToken<V>? = null


    var  container: PropertyContainer<T>? = null

    fun returnKey(clazz: KClass<V>): PropertyInfo<T, V>{
        returnTypeKey = StaticTypeKey.createTypeKey(clazz)
        return this
    }

    fun addReceiverInfo(container: PropertyContainer<T>){
        this.container = container
    }

    companion object{
        fun <T: Any, V: Any> create(kProperty1: KProperty1<T, V>,  clazz: KClass<T>):PropertyInfo<T, V>{
           return PropertyInfo(kProperty1, clazz)
        }
        fun <T: Any, V: Any> create(kProperty1: KProperty1<T, V>,  instance: T):PropertyInfo<T, V>{
            val clazz =  instance::class as KClass<T>
            val property = PropertyInfo(kProperty1, clazz)
            kProperty1.get(instance).let {
                val resultClass =  it::class as KClass<V>
                val staticTypeKey = StaticTypeKey<V>(resultClass)
                property.returnTypeKey = staticTypeKey
            }
            return property
        }
    }

}


fun <T : Any> StaticTypeToken<T>.assignIfMatches(
    map : Map<String, PropertyInfo<*, T>>,
) {
    map.values.forEach {
        if(it.returnType == this.kType){
            it.returnTypeToken = this
        }
    }
}



fun <T: Any, V: Any> KProperty1<T, V>.toPropertyInfo(clazz: KClass<T>):PropertyInfo<T, V>{

   val info =  PropertyInfo.create(this, clazz)
   return  info
}

fun <T: Any, V: Any> KMutableProperty1<T, V>.toPropertyInfo(clazz: KClass<T>):PropertyInfo<T, V>{
    val info = PropertyInfo(this,  clazz)
    return  info
}

//inline fun <reified T : Any> Any.toPropertyInfo(name: String):PropertyInfo<T, Any>?{
//   return  this::class.memberProperties.firstOrNull { it.name == name }?.let { property ->
//        property.getter.call()?.let { result ->
//            PropertyInfo(property.name, property.castOrThrow<KProperty1<T, Any>, ManagedException>() , result::class.createType())
//        }
//    }
//}

//
//inline fun <reified T : Any> Any.toPropertyInfo():PropertyInfo<T, Any>?{
//
////    return  this::class.objectInstance.t
////        property.getter.call()?.let { result ->
////            PropertyInfo(property.name, property.castOrThrow<KProperty1<T, Any>, ManagedException>() , result::class.createType())
////        }
////    }
//}