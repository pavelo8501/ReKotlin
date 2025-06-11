package po.misc.validators.mapping.helpers


import po.misc.validators.mapping.models.MappingCheck
import po.misc.validators.mapping.models.MappingCheckRecord
import po.misc.validators.mapping.models.ValidationClass
import po.misc.validators.mapping.models.ValidationRecord
import po.misc.validators.mapping.reports.ReportRecordDepr

fun <T: Any>  MappingCheck<T>.compareSame(
    mappingRecord: MappingCheckRecord,
    validatable: ValidationRecord
): ReportRecordDepr {

    val mappingPropertyRecord = mappingRecord.propertyRecord
    return if (mappingPropertyRecord != null) {
        ReportRecordDepr(mappingPropertyRecord.propertyName, validatable.propertyRecord.propertyName)
    } else {
        reportWarning("Mapping between column: ${mappingRecord.columnName} and its property failed, skipping", mappingRecord)
    }
}

fun <T: Any> MappingCheck<T>.containsSame(
    mappingRecord: MappingCheckRecord,
    validatable: ValidationClass<T>
): ReportRecordDepr {

    val mappingPropertyRecord =  mappingRecord.propertyRecord
    return if(mappingPropertyRecord != null){
        val validatableRecord = validatable.validatableRecords.firstOrNull { it.propertyRecord.propertyName == mappingPropertyRecord.propertyName }
        if(validatableRecord != null){
            ReportRecordDepr(mappingRecord.columnName, validatableRecord.propertyRecord.propertyName).setSuccess()
        }else{
            ReportRecordDepr(mappingRecord.columnName, "N/A").setFailure("Binding not configured for $mappingRecord.columnName")
        }
    }else{
        reportWarning("Mapping between column: ${mappingRecord.columnName} and its property failed, skipping", mappingRecord)
    }
}


fun MappingCheck<*>.reportFail(
    mappingRecord: MappingCheckRecord
): ReportRecordDepr {
    val mappingPropertyRecord = mappingRecord.propertyRecord
    return if (mappingPropertyRecord != null) {
        ReportRecordDepr(mappingRecord.columnName, "N/A").setFailure("Dummy failure")
    } else {
        reportWarning("Mapping between column: ${mappingRecord.columnName} and its property failed, skipping", mappingRecord)
    }
}

fun MappingCheck<*>.reportWarning(
    message: String,
    mappingRecord: MappingCheckRecord
): ReportRecordDepr {

    val mappingPropertyRecord = mappingRecord.propertyRecord
    return if (mappingPropertyRecord != null) {
        ReportRecordDepr(mappingPropertyRecord.toString(),  "N/A").setWarning(message)
    } else {
        reportWarning("Mapping between column: ${mappingRecord.columnName} and its property failed, skipping", mappingRecord)
    }
}

fun MappingCheck<*>.reportWarning(
    message: String,
    mappingRecords: List<MappingCheckRecord>
): List<ReportRecordDepr> {
    return mappingRecords.map { reportWarning(message, it) }
}




