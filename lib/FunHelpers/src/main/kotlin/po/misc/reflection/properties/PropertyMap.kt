package po.misc.reflection.properties

import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

open class PropertyMap<T: Any> {


    private val propertyBacking: MutableMap<String, KProperty1<T, *>> = mutableMapOf()
    val  properties: Map<String, KProperty1<T, *>> = propertyBacking

    val propertyList: List<KProperty1<T, *>> get () = propertyBacking.values.toList()

    val propertySize: Int get() = propertyBacking.size

    fun getPropertyByName(name: String):KProperty1<T, *>?{
       return propertyBacking[name]
    }

    fun addProperty(name: String,  kProperty: KProperty1<T, *>):KProperty1<T, *>{
        propertyBacking.put(name, kProperty)
        return kProperty
    }

}