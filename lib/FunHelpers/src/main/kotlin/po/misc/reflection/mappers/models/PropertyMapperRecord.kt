package po.misc.reflection.mappers.models

import po.misc.data.ColumnMetadata
import po.misc.reflection.mappers.interfaces.MappablePropertyRecord
import po.misc.types.TypeData
import po.misc.types.TypeRecord
import po.misc.types.Typed

class PropertyMapperRecord<T: Any>(
    override val classTypeRecord: Typed<T>,
    override val propertyMap: Map<String, PropertyRecord<T>>,
    override var columnMetadata:  List<ColumnMetadata> = emptyList()
): MappablePropertyRecord<T> {

}