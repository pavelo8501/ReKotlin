package po.exposify.dao.models


data class ColumnMetadata(
    val columnName: String,
    val type: String,
    val isNullable: Boolean,
    val isPrimaryKey: Boolean,
    val hasDefault: Boolean,
    val isAutoIncrement: Boolean,
    val isForeignKey: Boolean,
    val referencedTable: String? = null,
) {
    val isMandatory: Boolean get() = !isNullable && !hasDefault && !isPrimaryKey && !isForeignKey
}