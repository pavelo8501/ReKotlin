package po.misc.validators.models

import po.misc.reflection.properties.mappers.interfaces.MappablePropertyRecord
import po.misc.reflection.properties.mappers.models.PropertyRecord

data class MappingCheckRecord(
    val propertyRecord: PropertyRecord<*>?,
    val columnName: String,
    val type: String,
    val isNullable: Boolean,
    val isPrimaryKey: Boolean,
    val hasDefault: Boolean,
    val isAutoIncrement: Boolean,
    val isForeignKey: Boolean,
    val referencedTable: String?,
){

    var status: CheckStatus = CheckStatus.IDLE
        private set
    var message: String = "Check never run"
        private set

    override fun toString(): String {
       return columnName
    }

}

enum class CheckStatus {
    PASSED, WARNING, FAILED, IDLE
}