package po.misc.data

import po.misc.reflection.properties.mappers.models.PropertyRecord
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1



data class ColumnMetadata(
    val columnName: String,
    val type: String,
    val isNullable: Boolean,
    val isPrimaryKey: Boolean,
    val hasDefault: Boolean,
    val isAutoIncrement: Boolean,
    val isForeignKey: Boolean,
    val referencedTable: String? = null,
    var propertyRecord: PropertyRecord<*>? = null
)