package po.misc.validators.helpers


import po.misc.validators.models.MappingCheck
import po.misc.validators.models.MappingCheckRecord
import po.misc.validators.reports.ReportRecord
import po.misc.validators.models.ValidationClass
import po.misc.validators.models.ValidationRecord

fun <T: Any>  MappingCheck<T>.compareSame(
    mappingRecord: MappingCheckRecord,
    validatable: ValidationRecord
): ReportRecord {

    val mappingPropertyRecord = mappingRecord.propertyRecord
    return if (mappingPropertyRecord != null) {
        ReportRecord(mappingPropertyRecord.propertyName, validatable.propertyRecord.propertyName)
    } else {
        reportWarning("Mapping between column: ${mappingRecord.columnName} and its property failed, skipping", mappingRecord)
    }
}

fun <T: Any> MappingCheck<T>.containsSame(
    mappingRecord: MappingCheckRecord,
    validatable: ValidationClass<T>
): ReportRecord {

    val mappingPropertyRecord =  mappingRecord.propertyRecord
    return if(mappingPropertyRecord != null){
        val validatableRecord = validatable.validatableRecords.firstOrNull { it.propertyRecord.propertyName == mappingPropertyRecord.propertyName }
        if(validatableRecord != null){
            ReportRecord(mappingRecord.columnName, validatableRecord.propertyRecord.propertyName).setSuccess()
        }else{
            ReportRecord(mappingRecord.columnName, "N/A").setFailure("Binding not configured for $mappingRecord.columnName")
        }
    }else{
        reportWarning("Mapping between column: ${mappingRecord.columnName} and its property failed, skipping", mappingRecord)
    }
}


fun MappingCheck<*>.reportFail(
    mappingRecord: MappingCheckRecord
): ReportRecord {
    val mappingPropertyRecord = mappingRecord.propertyRecord
    return if (mappingPropertyRecord != null) {
        ReportRecord(mappingRecord.columnName, "N/A").setFailure("Dummy failure")
    } else {
        reportWarning("Mapping between column: ${mappingRecord.columnName} and its property failed, skipping", mappingRecord)
    }
}

fun MappingCheck<*>.reportWarning(
    message: String,
    mappingRecord: MappingCheckRecord
): ReportRecord {

    val mappingPropertyRecord = mappingRecord.propertyRecord
    return if (mappingPropertyRecord != null) {
        ReportRecord(mappingPropertyRecord.toString(),  "N/A").setWarning(message)
    } else {
        reportWarning("Mapping between column: ${mappingRecord.columnName} and its property failed, skipping", mappingRecord)
    }
}

fun MappingCheck<*>.reportWarning(
    message: String,
    mappingRecords: List<MappingCheckRecord>
): List<ReportRecord> {
    return mappingRecords.map { reportWarning(message, it) }
}




