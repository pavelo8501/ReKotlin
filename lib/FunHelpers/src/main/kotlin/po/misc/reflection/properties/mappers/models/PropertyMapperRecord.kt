package po.misc.reflection.properties.mappers.models

import po.misc.data.ColumnMetadata
import po.misc.reflection.properties.mappers.interfaces.MappablePropertyRecord
import po.misc.types.TypeRecord

class PropertyMapperRecord<T: Any>(
    override val classTypeRecord: TypeRecord<T>,
    override val propertyMap: Map<String, PropertyRecord<T>>,
    override var columnMetadata:  List<ColumnMetadata> = emptyList()
): MappablePropertyRecord<T> {

}