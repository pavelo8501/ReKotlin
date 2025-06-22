package po.misc.reflection.mappers.models

import po.misc.data.ColumnMetadata
import po.misc.reflection.mappers.interfaces.MappablePropertyRecord
import po.misc.types.TypeRecord

class PropertyMapperRecord<T: Any>(
    override val classTypeRecord: TypeRecord<T>,
    override val propertyMap: Map<String, PropertyRecord<T>>,
    override var columnMetadata:  List<ColumnMetadata> = emptyList()
): MappablePropertyRecord<T> {

}