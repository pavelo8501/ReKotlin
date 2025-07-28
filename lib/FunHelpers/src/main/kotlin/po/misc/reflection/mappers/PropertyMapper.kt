package po.misc.reflection.mappers

import po.misc.exceptions.ManagedException
import po.misc.interfaces.ValueBased
import po.misc.reflection.mappers.models.PropertyMapperRecord
import po.misc.reflection.mappers.models.PropertyRecord
import po.misc.reflection.properties.toPropertyMap
import po.misc.types.TypeData
import po.misc.types.castBaseOrThrow
import po.misc.types.castOrManaged
import po.misc.types.safeCast


class PropertyMapper {

    /* Multidimensional Map containing Class based CompositeKey, and sub map of PropertyRecords by property names */
    val mappedProperties : MutableMap<ValueBased, PropertyMapperRecord<*>> = mutableMapOf()


    inline fun <reified T: Any> applyClass(key: ValueBased): PropertyMapperRecord<T> {
        val typeRecord = TypeData.create<T>()
        val propertyMap = toPropertyMap<T>()
        return mappedProperties[key]?.let { existent ->
            val asMutable = existent.propertyMap.toMutableMap()
            propertyMap.forEach { (key, value) ->
                asMutable[key] = value
            }
            existent.castOrManaged<PropertyMapperRecord<T>>(this)
        } ?: run {
            val newRecord = PropertyMapperRecord(typeRecord, propertyMap)
            mappedProperties[key] = newRecord
            newRecord
        }
    }

    fun addMapperRecord(key: ValueBased, item : PropertyMapperRecord<*>){
        mappedProperties[key] = item
    }

    @JvmName("getPropertyItemUnsafe")
    inline fun <T: Any, reified E: ManagedException> getMapperRecord(key: ValueBased, exceptionProvider:(String)->E): PropertyMapperRecord<T> {
        return mappedProperties[key].castBaseOrThrow<PropertyMapperRecord<T>, E>(null,  exceptionProvider)
    }

    fun <T: Any> getMapperRecord(key: ValueBased): PropertyMapperRecord<T>?{
        return mappedProperties[key]?.safeCast()
    }

    fun  getPropertyRecord(key: ValueBased, propertyName: String):  PropertyRecord<*>?{
       return mappedProperties[key]?.let {
           it.propertyMap[propertyName]
       }
    }
}