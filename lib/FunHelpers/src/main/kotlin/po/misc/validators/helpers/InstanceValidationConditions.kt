package po.misc.validators.helpers

import po.misc.validators.models.InstanceRecord
import po.misc.validators.models.InstancedCheckV2
import po.misc.validators.models.MappingCheckRecord
import po.misc.validators.models.MappingCheckV2
import po.misc.validators.models.ReportRecord
import po.misc.validators.models.ValidationInstance



fun <T: Any> InstancedCheckV2<T>.conditionTrue(
    validatable: InstanceRecord<T>,
    failureMessage: String,
    predicate: (T)-> Boolean
): ReportRecord {
    val result = predicate.invoke(validatable.instance)
    val reportRecord = ReportRecord(validatable.propertyRecord, "N/A", null)
    return if (result) {
        reportRecord.setSuccess()
    } else {
        reportRecord.setFailure(failureMessage)
    }
}

fun InstancedCheckV2<*>.reportWarning(
    message: String,
    mappingRecord: MappingCheckRecord
): ReportRecord {

    val mappingPropertyRecord = mappingRecord.propertyRecord
    return if (mappingPropertyRecord != null) {
        ReportRecord(mappingPropertyRecord, mappingRecord.columnName, null).setWarning(message)
    } else {
        reportWarning("Mapping between column: ${mappingRecord.columnName} and its property failed, skipping", mappingRecord)
    }
}

fun InstancedCheckV2<*>.reportWarning(
    message: String,
    mappingRecords: List<MappingCheckRecord>
): List<ReportRecord> {
    return mappingRecords.map { reportWarning(message, it) }
}

