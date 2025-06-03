package po.misc.validators.helpers

import po.misc.validators.models.InstanceRecord
import po.misc.validators.models.InstancedCheck
import po.misc.validators.models.MappingCheck
import po.misc.validators.models.MappingCheckRecord
import po.misc.validators.reports.ReportRecord
import po.misc.validators.models.ValidationClass
import po.misc.validators.models.ValidationRecord

fun <T: Any> MappingCheck<T>.bulkValidator(
    failureMessage: String = "",
    predicate: MappingCheck<T>.(testable: List<MappingCheckRecord>, toMapping:ValidationClass<T>)-> ReportRecord
):MappingCheck<T> {
    errorMessage = failureMessage
    bulkPredicate = predicate
    return this
}

fun <T: Any>   MappingCheck<T>.sequentialBySource(
    failureMessage: String = "",
    predicate: MappingCheck<T>.(sourceRecord: MappingCheckRecord, testableData: ValidationClass<T>)-> ReportRecord
): MappingCheck<T>{
    errorMessage = failureMessage
    sequentialBySource = predicate
    return this
}


fun <T: Any> MappingCheck<T>.sequentialByValidatable(
    failureMessage: String = "",
    predicate: MappingCheck<T>.(validatable: ValidationRecord,  sourceRecord: List<MappingCheckRecord>)-> ReportRecord
): MappingCheck<T>{
    errorMessage = failureMessage
    sequentialByValidatable = predicate
    return this
}

fun <T: Any> InstancedCheck<T>.sequentialByInstance(
    failureMessage: String = "",
    predicate: InstancedCheck<T>.(validatable: InstanceRecord<T>, sourceRecord: List<MappingCheckRecord>)-> ReportRecord
): InstancedCheck<T>{
    errorMessage = failureMessage
    sequentialByInstance = predicate
    return this
}

