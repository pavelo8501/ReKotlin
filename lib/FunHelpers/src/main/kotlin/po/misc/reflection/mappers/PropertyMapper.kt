package po.misc.reflection.mappers

import po.misc.exceptions.ManagedException
import po.misc.interfaces.Identifiable
import po.misc.interfaces.ValueBased
import po.misc.reflection.mappers.helpers.toMappingCheckRecords
import po.misc.reflection.mappers.models.PropertyMapperRecord
import po.misc.reflection.mappers.models.PropertyRecord
import po.misc.reflection.properties.toPropertyMap
import po.misc.types.TypeRecord
import po.misc.types.castBaseOrThrow
import po.misc.types.castOrThrow
import po.misc.types.getOrThrow
import po.misc.types.safeCast
import po.misc.validators.mapping.MappingValidator
import po.misc.validators.mapping.models.InstancedCheck
import po.misc.validators.mapping.models.MappingCheck
import po.misc.validators.mapping.models.MappingCheckRecord
import po.misc.validators.mapping.reports.MappingReport

class PropertyMapper {

    /* Multidimensional Map containing Class based CompositeKey, and sub map of PropertyRecords by property names */
    val mappedProperties : MutableMap<ValueBased, PropertyMapperRecord<*>> = mutableMapOf()
    val propertyValidator : MappingValidator = MappingValidator()

    fun <T: Any> requestSourceData(
        key: ValueBased,
        component: Identifiable,
        requestType: MappingValidator.MappedPropertyValidator
    ):List<MappingCheckRecord>{

        val mapperRecord =  getMapperRecord<T>(key).getOrThrow<PropertyMapperRecord<T>, ManagedException>()

        return  when(requestType){
            MappingValidator.MappedPropertyValidator.NON_NULLABLE->{
                mapperRecord.columnMetadata.filter { !it.isNullable && !it.hasDefault && !it.isForeignKey }.toMappingCheckRecords(mapperRecord)
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
        check : MappingCheck<T>,
        validatorType: MappingValidator.MappedPropertyValidator
    ): MappingReport {
        val sourceRecords = requestSourceData<T>(check.sourceKey, check.component, validatorType)
        check.setMappings(sourceRecords)
        check.validatorType = validatorType
        return propertyValidator.executeCheck(check, sourceRecords)
    }

    fun <T: Any> executeCheck(
        check : InstancedCheck<T>,
        validatorType: MappingValidator.MappedPropertyValidator
    ): MappingReport {
        val sourceRecords = requestSourceData<T>(check.sourceKey, check.component, validatorType)
        check.setMappings(sourceRecords)
        check.validatorType = validatorType
        return propertyValidator.executeCheck(check, sourceRecords)
    }

    inline fun <reified T: Any> applyClass(key: ValueBased): PropertyMapperRecord<T> {
        val typeRecord = TypeRecord.Companion.createRecord<T>(key)
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

    fun addMapperRecord(key: ValueBased, item : PropertyMapperRecord<*>){
        mappedProperties[key] = item
    }

    @JvmName("getPropertyItemUnsafe")
    inline fun <T: Any, reified E: ManagedException> getMapperRecord(key: ValueBased): PropertyMapperRecord<T> {
        return mappedProperties[key].castBaseOrThrow<PropertyMapperRecord<T>, E>()
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