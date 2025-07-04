package po.misc.validators.mapping.helpers

import po.misc.validators.mapping.models.InstanceRecord
import po.misc.validators.mapping.models.InstancedCheck
import po.misc.validators.mapping.models.MappingCheck
import po.misc.validators.mapping.models.MappingCheckRecord
import po.misc.validators.mapping.models.ValidationClass
import po.misc.validators.mapping.models.ValidationRecord
import po.misc.validators.mapping.reports.ReportRecordDepr


fun <T: Any> MappingCheck<T>.bulkValidator(
    failureMessage: String = "",
    predicate: MappingCheck<T>.(testable: List<MappingCheckRecord>, toMapping:ValidationClass<T>)-> ReportRecordDepr
):MappingCheck<T> {
    errorMessage = failureMessage
    bulkPredicate = predicate
    return this
}

fun <T: Any>  MappingCheck<T>.sequentialBySource(
    failureMessage: String = "",
    predicate: MappingCheck<T>.(sourceRecord: MappingCheckRecord, testableData: ValidationClass<T>)-> ReportRecordDepr
): MappingCheck<T>{
    errorMessage = failureMessage
    sequentialBySource = predicate
    return this
}


fun <T: Any> MappingCheck<T>.sequentialByValidatable(
    failureMessage: String = "",
    predicate: MappingCheck<T>.(validatable: ValidationRecord,  sourceRecord: List<MappingCheckRecord>)-> ReportRecordDepr
): MappingCheck<T>{
    errorMessage = failureMessage
    sequentialByValidatable = predicate
    return this
}

fun <T: Any> InstancedCheck<T>.sequentialByInstance(
    failureMessage: String = "",
    predicate: InstancedCheck<T>.(validatable: InstanceRecord<T>, sourceRecord: List<MappingCheckRecord>)-> ReportRecordDepr
): InstancedCheck<T>{
    errorMessage = failureMessage
    sequentialByInstance = predicate
    return this
}

