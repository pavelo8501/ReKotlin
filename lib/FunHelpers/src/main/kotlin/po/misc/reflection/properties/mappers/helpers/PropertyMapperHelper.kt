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
import po.misc.validators.models.InstancedCheckV2
import po.misc.validators.models.MappingCheckRecord
import po.misc.validators.models.MappingCheckV2
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
    mapperItem:  MappablePropertyRecord<*>,
    validationObject: ValidationClass<T>
): MappingCheckV2<T>{
    val validator = this.propertyValidator
    return MappingCheckV2<T>(validationObject.component, mapperItem.classTypeRecord.component1(), validationObject, validator)
}

fun <T: Any>  PropertyMapper.mapperInstancedCheck(
    mapperItem:  MappablePropertyRecord<*>,
    validationInstance: ValidationInstance<T>
): InstancedCheckV2<T>{
    val validator = this.propertyValidator
    return InstancedCheckV2(validationInstance.component, mapperItem.classTypeRecord.component1(), validationInstance.records, validator)
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




