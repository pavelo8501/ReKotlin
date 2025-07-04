package po.misc.validators.mapping.helpers

import po.misc.validators.mapping.models.InstanceRecord
import po.misc.validators.mapping.models.InstancedCheck
import po.misc.validators.mapping.models.MappingCheckRecord
import po.misc.validators.mapping.reports.MappingReportDepr
import po.misc.validators.mapping.reports.ReportRecordDepr


//fun <T: Any> InstancedCheck<T>.conditionTrue(
//    validatable: InstanceRecord<T>,
//    failureMessage: String,
//    predicate: T.()-> Boolean
//): MappingReportDepr {
//    val result = predicate.invoke(validatable.instance)
//    val reportRecord = MappingReportDepr(validatable.propertyRecord.propertyName, "N/A")
//    return if (result) {
//        reportRecord.setSuccess()
//    } else {
//        reportRecord.setFailure(failureMessage)
//    }
//}

fun InstancedCheck<*>.reportWarning(
    message: String,
    mappingRecord: MappingCheckRecord
): ReportRecordDepr {

    val mappingPropertyRecord = mappingRecord.propertyRecord
    return if (mappingPropertyRecord != null) {
        ReportRecordDepr(mappingPropertyRecord.propertyName, mappingRecord.columnName).setWarning(message)
    } else {
        reportWarning("Mapping between column: ${mappingRecord.columnName} and its property failed, skipping", mappingRecord)
    }
}

fun InstancedCheck<*>.reportWarning(
    message: String,
    mappingRecords: List<MappingCheckRecord>
): List<ReportRecordDepr> {
    return mappingRecords.map { reportWarning(message, it) }
}

