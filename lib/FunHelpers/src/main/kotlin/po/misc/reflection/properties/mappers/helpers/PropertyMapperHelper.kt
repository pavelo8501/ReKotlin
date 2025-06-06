package po.misc.reflection.properties.mappers.helpers

import po.misc.data.ColumnMetadata
import po.misc.exceptions.ManagedException
import po.misc.interfaces.ValueBased
import po.misc.reflection.properties.PropertyMapper
import po.misc.reflection.properties.mappers.interfaces.MappablePropertyRecord
import po.misc.reflection.properties.mappers.models.PropertyMapperRecord
import po.misc.reflection.properties.mappers.models.PropertyRecord
import po.misc.types.TypeRecord
import po.misc.types.castOrThrow
import po.misc.types.safeCast
import po.misc.validators.MappingValidator
import po.misc.validators.models.InstancedCheck
import po.misc.validators.models.MappingCheckRecord
import po.misc.validators.models.MappingCheck
import po.misc.validators.models.ValidationClass
import po.misc.validators.models.ValidationInstance
import kotlin.reflect.KProperty
import kotlin.reflect.full.memberProperties


fun <T : Any> createPropertyMap(
    typeRecord: TypeRecord<T>,
    columnMetadata: List<ColumnMetadata>? = null
): PropertyMapperRecord<T>{
    return PropertyMapperRecord(
        classTypeRecord = typeRecord,
        columnMetadata = columnMetadata?:emptyList(),
        propertyMap = typeRecord.clazz.memberProperties.associate {
          it.name to PropertyRecord(it.name, it.castOrThrow<KProperty<T>, ManagedException>())
    })
}

inline fun <reified T : Any> createPropertyMap(
    element: ValueBased,
    columnMetadata: List<ColumnMetadata>? = null
): PropertyMapperRecord<T>{
    val typeRecord = TypeRecord.createRecord<T>(element)
    val properties =  T::class.memberProperties.associate {
        it.name to PropertyRecord(it.name, it.castOrThrow<KProperty<T>,  ManagedException>("Cast to KProperty failed at PropertyMap<T: Any>"))
    }
    return PropertyMapperRecord(propertyMap = properties, classTypeRecord = typeRecord, columnMetadata = columnMetadata?:emptyList())
}

fun <T: Any>  PropertyMapper.mapperCheck(
    checkName: String,
    mapperItem:  MappablePropertyRecord<*>,
    validationObject: ValidationClass<T>
): MappingCheck<T>{
    return MappingCheck(checkName, validationObject.component,  mapperItem.classTypeRecord.component1(), validationObject)
}

fun <T: Any>  PropertyMapper.mapperCheck(
    checkName: String,
    mapperItem:  MappablePropertyRecord<*>,
    validationInstance: ValidationInstance<T>
): InstancedCheck<T>{
    return InstancedCheck(checkName, validationInstance.component, mapperItem.classTypeRecord.component1(), validationInstance)
}

fun List<ColumnMetadata>.toMappingCheckRecords(
    mapperRecord: PropertyMapperRecord<*>
): List<MappingCheckRecord>
{
    val result = mutableListOf<MappingCheckRecord>()
    forEach { columnRecord->
       val mapping = MappingCheckRecord(
            propertyRecord = columnRecord.propertyRecord,
            columnName = columnRecord.columnName,
            type =   columnRecord.type,
            isNullable =  columnRecord.isNullable,
            isPrimaryKey =  columnRecord.isPrimaryKey,
            hasDefault =  columnRecord.hasDefault,
            isAutoIncrement =  columnRecord.isAutoIncrement,
            isForeignKey =  columnRecord.isForeignKey,
            referencedTable = columnRecord.referencedTable
        )
        result.add(mapping)
    }
    return result
}




