package po.misc.validators.helpers


import po.misc.validators.models.InstancedCheckV2
import po.misc.validators.models.MappingCheckV2
import po.misc.validators.models.MappingCheckRecord
import po.misc.validators.models.ReportRecord
import po.misc.validators.models.ValidationClass
import po.misc.validators.models.ValidationRecord

fun <T: Any>  MappingCheckV2<T>.compareSame(
    mappingRecord: MappingCheckRecord,
    validatable: ValidationRecord
): ReportRecord {

    val mappingPropertyRecord = mappingRecord.propertyRecord
    return if (mappingPropertyRecord != null) {
        ReportRecord(mappingPropertyRecord, "", validatable.propertyRecord)
    } else {
        reportWarning("Mapping between column: ${mappingRecord.columnName} and its property failed, skipping", mappingRecord)
    }
}

fun <T: Any> MappingCheckV2<T>.containsSame(
    mappingRecord: MappingCheckRecord,
    validatable: ValidationClass<T>
): ReportRecord {

    val mappingPropertyRecord =  mappingRecord.propertyRecord
    return if(mappingPropertyRecord != null){
        val validatableRecord = validatable.records.firstOrNull { it.propertyRecord.propertyName == mappingPropertyRecord.propertyName }
        if(validatableRecord != null){
            ReportRecord(mappingPropertyRecord, mappingRecord.columnName, validatableRecord.propertyRecord).setSuccess()
        }else{
            ReportRecord(mappingPropertyRecord, mappingRecord.columnName, null).setFailure("Binding not configured for $mappingRecord.columnName")
        }
    }else{
        reportWarning("Mapping between column: ${mappingRecord.columnName} and its property failed, skipping", mappingRecord)
    }
}


fun MappingCheckV2<*>.reportFail(
    mappingRecord: MappingCheckRecord
): ReportRecord {
    val mappingPropertyRecord = mappingRecord.propertyRecord
    return if (mappingPropertyRecord != null) {
        ReportRecord(
            mappingPropertyRecord,
            mappingRecord.columnName,
            null
        ).setFailure("Dummy failure")
    } else {
        reportWarning("Mapping between column: ${mappingRecord.columnName} and its property failed, skipping", mappingRecord)
    }
}

fun MappingCheckV2<*>.reportWarning(
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

fun MappingCheckV2<*>.reportWarning(
    message: String,
    mappingRecords: List<MappingCheckRecord>
): List<ReportRecord> {
    return mappingRecords.map { reportWarning(message, it) }
}




