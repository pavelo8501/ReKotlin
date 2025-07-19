package po.misc.reflection.classes

import po.misc.context.CTX
import po.misc.reflection.properties.PropertyGroup
import po.misc.reflection.properties.PropertyIO
import kotlin.reflect.KClass
import kotlin.reflect.KType

abstract class ContainerBase<T>(
    internal val kClass: KClass<T>,
    internal val kType: KType
): CTX where T: ContainerBase<T>{

    abstract val  self: ContainerBase<T>

    override val contextName: String
        get() = "ContainerBase"


    internal var dataInstance:T? = null

    val propertyMap: MutableMap<String, PropertyIO<T, Any>> = mutableMapOf()
    //val propertySlots: MutableMap<String, PropertySlot<T, Any>> = mutableMapOf()
    val propertyGroups: MutableMap<String, PropertyGroup<*, Any>> = mutableMapOf()

    fun provideData(data:T) {
        dataInstance = data


//        propertyGroups.values.forEach {
//            val slot = it.getSlot(kClass)
//            val value = slot.property.extractValue(data)
//             it.update(value, slot)
//        }

    }

    fun extractData():T?{
        return dataInstance
    }

//    fun provideGroup(group: PropertyGroup<*, Any>){
//        propertyGroups.put(group.sourceProperty.propertyName, group)
//    }
//
//    fun registerSlot(slot: PropertySlot<T, Any> ){
//
//        propertySlots.put(slot.name, slot)
//        propertyMap.put(slot.property.propertyName, slot.property)
//
//    }

}
