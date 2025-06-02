package po.misc.reflection.properties.mappers.interfaces

import po.misc.data.ColumnMetadata
import po.misc.reflection.properties.mappers.models.PropertyRecord
import po.misc.types.TypeRecord

interface MappablePropertyRecord<T: Any> {
    val classTypeRecord: TypeRecord<T>
    val propertyMap: Map<String, PropertyRecord<T>>
    val columnMetadata:  List<ColumnMetadata>?
}