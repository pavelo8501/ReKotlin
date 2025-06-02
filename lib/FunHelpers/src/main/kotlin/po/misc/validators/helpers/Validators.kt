package po.misc.validators.helpers

import po.misc.validators.models.InstanceRecord
import po.misc.validators.models.InstancedCheckV2
import po.misc.validators.models.MappingCheckV2
import po.misc.validators.models.MappingCheckRecord
import po.misc.validators.models.ReportRecord
import po.misc.validators.models.ValidationClass
import po.misc.validators.models.ValidationRecord

fun <T: Any> MappingCheckV2<T>.bulkValidator(
    failureMessage: String = "",
    predicate: MappingCheckV2<T>.(testable: List<MappingCheckRecord>, toMapping:ValidationClass<T>)-> ReportRecord
):MappingCheckV2<T> {
    errorMessage = failureMessage
    bulkPredicate = predicate
    return this
}

fun <T: Any>   MappingCheckV2<T>.sequentialBySource(
    failureMessage: String = "",
    predicate: MappingCheckV2<T>.(sourceRecord: MappingCheckRecord, testableData: ValidationClass<T>)-> ReportRecord
): MappingCheckV2<T>{
    errorMessage = failureMessage
    sequentialBySource = predicate
    return this
}


fun <T: Any> MappingCheckV2<T>.sequentialByValidatable(
    failureMessage: String = "",
    predicate: MappingCheckV2<T>.(validatable: ValidationRecord,  sourceRecord: List<MappingCheckRecord>)-> ReportRecord
): MappingCheckV2<T>{
    errorMessage = failureMessage
    sequentialByValidatable = predicate
    return this
}

fun <T: Any> InstancedCheckV2<T>.sequentialByInstance(
    failureMessage: String = "",
    predicate: InstancedCheckV2<T>.(validatable: InstanceRecord<T>, sourceRecord: List<MappingCheckRecord>)-> ReportRecord
): InstancedCheckV2<T>{
    errorMessage = failureMessage
    sequentialByInstance = predicate
    return this
}

