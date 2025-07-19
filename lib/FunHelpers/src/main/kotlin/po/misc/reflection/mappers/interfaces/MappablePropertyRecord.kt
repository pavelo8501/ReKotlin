package po.misc.reflection.mappers.interfaces

import po.misc.data.ColumnMetadata
import po.misc.reflection.mappers.models.PropertyRecord
import po.misc.types.TypeData
import po.misc.types.TypeRecord
import po.misc.types.Typed

interface MappablePropertyRecord<T: Any> {
    val classTypeRecord: Typed<T>
    val propertyMap: Map<String, PropertyRecord<T>>
    val columnMetadata:  List<ColumnMetadata>?
}