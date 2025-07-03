package po.misc.reflection.properties

import po.misc.interfaces.IdentifiableContext
import po.misc.reflection.properties.enums.DataTag


class PropertyGroup<T : Any, V : Any>(){

    val propertySlots: MutableMap<DataTag, PropertyIO<*, V>> = mutableMapOf()

    var onProvideData: ((V)-> Unit)? = null
    fun provideDataLambda(block: (V)-> Unit){
        onProvideData = block
    }

    fun <T2: Any> update(value: V, container: T2):V{
        return value
    }

    fun update(value:V):V{
        return value
    }

    fun <T2 : Any> getForeignSlots(receiver: T2): List<PropertyIO<*, V>> {
        return propertySlots.values.filter { !it.typeKey.isInstanceOfType(receiver) }
    }
}


