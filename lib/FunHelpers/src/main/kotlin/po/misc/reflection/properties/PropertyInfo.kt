package po.misc.reflection.properties


import po.misc.data.helpers.textIfNull
import po.misc.types.toSimpleNormalizedKey
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.KVisibility
import kotlin.reflect.typeOf

class StaticTypeToken<T : Any>(
    val value: T,
    val kType: KType,
    val propertyName: String? = null
)

data class PropertyInfo<T: Any>(
    val name: String,
    val property: KProperty1<T, Any>,
    val returnType: KType,
    val visibility: KVisibility?,
){
    var returnTypeToken : StaticTypeToken<*>? = null

    var  container: PropertyContainer<T>? = null

    fun addReceiverInfo(container: PropertyContainer<T>){
        this.container = container
    }
}

inline fun <reified T : Any> typeTokenOf(value: T): StaticTypeToken<T> {
    val property = value::class.simpleName.toString()
    val type = typeOf<T>()
    return  StaticTypeToken<T>(value, type, property)
}

fun <T : Any> StaticTypeToken<T>.assignIfMatches(
    map : Map<String, PropertyInfo<*>>,
) {
    map.values.forEach {
        if(it.returnType == this.kType){
            it.returnTypeToken = this
        }
    }
}



fun <T: Any> KProperty1<T, Any>.toPropertyInfo(receiver:T, container:PropertyContainer<T>? = null):PropertyInfo<T>{

   val info = PropertyInfo( this.name, this,  this.returnType, this.visibility)
   if(container != null){
       container.addProperty(info)
       info.addReceiverInfo(container)
   }
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