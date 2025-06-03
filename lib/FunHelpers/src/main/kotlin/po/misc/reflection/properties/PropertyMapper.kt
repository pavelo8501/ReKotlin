package po.misc.reflection.properties

import po.misc.exceptions.ManagedException
import po.misc.interfaces.Identifiable
import po.misc.interfaces.ValueBased
import po.misc.reflection.properties.mappers.helpers.toMappingCheckRecords
import po.misc.reflection.properties.mappers.models.PropertyMapperRecord
import po.misc.reflection.properties.mappers.models.PropertyRecord
import po.misc.validators.MappingValidator
import po.misc.types.TypeRecord
import po.misc.types.castBaseOrThrow
import po.misc.types.castOrThrow
import po.misc.types.getOrThrow
import po.misc.types.safeCast
import po.misc.validators.models.ClassMappingReport
import po.misc.validators.models.InstancedCheckV2
import po.misc.validators.models.MappingCheckRecord
import po.misc.validators.models.MappingCheckV2
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set


class PropertyMapper {

    /* Multidimensional Map containing Class based CompositeKey, and sub map of PropertyRecords by property names */
    val mappedProperties : MutableMap<ValueBased, PropertyMapperRecord<*>> = mutableMapOf()
    val propertyValidator :MappingValidator = MappingValidator()

    private fun <T: Any> requestSourceData(
        key: ValueBased,
        component: Identifiable,
        requestType: MappingValidator.MappedPropertyValidator
    ):List<MappingCheckRecord>{

        val mapperRecord =  getMapperRecord<T>(key).getOrThrow<PropertyMapperRecord<T>, ManagedException>()

        return  when(requestType){
            MappingValidator.MappedPropertyValidator.NON_NULLABLE->{
                mapperRecord.columnMetadata.filter { !it.isNullable && !it.hasDefault }.toMappingCheckRecords(mapperRecord)
            }
            MappingValidator.MappedPropertyValidator.PARENT_SET->{
                mapperRecord.columnMetadata.filter { it.isForeignKey }.toMappingCheckRecords(mapperRecord)
            }
            MappingValidator.MappedPropertyValidator.FOREIGN_SET->{
                mapperRecord.columnMetadata.filter { it.isForeignKey }.toMappingCheckRecords(mapperRecord)
            }
        }
    }

    fun <T: Any> executeCheck(
        check : MappingCheckV2<T>,
        validatorType: MappingValidator.MappedPropertyValidator
    ):ClassMappingReport {
        val sourceRecords = requestSourceData<T>(check.fromKey, check.component, validatorType)
        check.validatorType = validatorType
        return propertyValidator.executeCheck(check, sourceRecords)
    }

    fun <T: Any> executeCheck(
        check : InstancedCheckV2<T>,
        validatorType: MappingValidator.MappedPropertyValidator
    ):ClassMappingReport {
        val sourceRecords = requestSourceData<T>(check.fromKey, check.component, validatorType)
        check.validatorType = validatorType
        return propertyValidator.executeCheck(check, sourceRecords)
    }

    inline fun <reified T: Any> applyClass(key: ValueBased): PropertyMapperRecord<T> {
        val typeRecord = TypeRecord.createRecord<T>(key)
        val propertyMap = toPropertyMap<T>()
        return mappedProperties[key]?.let { existent ->
            val asMutable = existent.propertyMap.toMutableMap()
            propertyMap.forEach { (key, value) ->
                asMutable[key] = value
            }
            existent.castOrThrow<PropertyMapperRecord<T>, ManagedException>()
        } ?: run {
            val newRecord = PropertyMapperRecord(typeRecord, propertyMap)
            mappedProperties[key] = newRecord
            newRecord
        }
    }

    fun addMapperRecord(key: ValueBased,  item : PropertyMapperRecord<*>){
        mappedProperties[key] = item
    }

    @JvmName("getPropertyItemUnsafe")
    inline fun <T: Any, reified E: ManagedException> getMapperRecord(key: ValueBased):PropertyMapperRecord<T>{
        return mappedProperties[key].castBaseOrThrow<PropertyMapperRecord<T>, E>()
    }

    fun <T: Any> getMapperRecord(key: ValueBased):PropertyMapperRecord<T>?{
        return mappedProperties[key]?.safeCast()
    }

    fun  getPropertyRecord(key: ValueBased, propertyName: String):  PropertyRecord<*>?{
       return mappedProperties[key]?.let {
           it.propertyMap[propertyName]
       }
    }
}
