package po.misc.reflection.properties

import po.misc.interfaces.ValueBased
import po.misc.registries.type.TypeRecord
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.memberProperties

class PropertyMap {

    /* Multidimensional Map containing Class based CompositeKey, and sub map of PropertyRecords by property names */
    val mappedProperties : MutableMap<ValueBased, Map<String, PropertyRecord<*, Any?>>> = mutableMapOf()

    val validator = MappingValidator(mappedProperties)

    @JvmName("applyClassFiltering")
    inline fun <T: Any, reified F: Any> applyClass(component: ValueBased, typeRecord : TypeRecord<T>){

        val filteredList  = typeRecord.clazz.findPropertiesOfType<T, F>().associate {
            it.name to PropertyRecord(it.name, it as KProperty<Any?>, typeRecord)
        }
        mappedProperties[component]?.let { existent ->
            val asMutable = existent.toMutableMap()
            filteredList.forEach { (key, value) ->
                asMutable[key] = value
            }
            mappedProperties[component] = asMutable
        } ?: run {
            mappedProperties[component] = filteredList
        }
    }

    inline fun <reified T: Any> applyClass(component: ValueBased, clazz: KClass<T>){
        val typeRecord = TypeRecord.Companion.createRecord<T>(component)
        val propertyList = clazz.memberProperties.associate {
            it.name to PropertyRecord(it.name, it as KProperty<Any?>, typeRecord)
        }
        mappedProperties[component] = propertyList
    }

    fun <T: Any> applyClass(component: ValueBased, clazz: KClass<T>, typeRecord: TypeRecord<T>){
        val propertyList = clazz.memberProperties.associate {
            it.name to PropertyRecord(it.name, it as KProperty<Any?>, typeRecord)
        }
        mappedProperties[component] = propertyList
    }

    fun <T: Any> provideMap(component: ValueBased, propertyMap :  Map<String, PropertyRecord<T, Any?>>){
        mappedProperties[component]?.let { existent ->
            val asMutable = existent.toMutableMap()
            propertyMap.forEach { (key, value) ->
                asMutable[key] = value
            }
        }?:run {
            mappedProperties[component] = propertyMap
        }
    }
}

data class PropertyRecord<T: Any, V>(val propertyName: String, val property: KProperty<V>, val typeRecord : TypeRecord<T>)