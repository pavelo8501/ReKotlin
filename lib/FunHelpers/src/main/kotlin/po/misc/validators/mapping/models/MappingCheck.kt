package po.misc.validators.mapping.models

import po.misc.exceptions.ManagedException
import po.misc.interfaces.Identifiable
import po.misc.interfaces.ValueBased
import po.misc.validators.mapping.MappingValidator
import po.misc.validators.mapping.reports.ReportRecordDepr
import kotlin.collections.List


/***
 * component: Identifiable To identify  who supplied data for checking
 * from  : PropertyMapperItem<T1> property map to be used
 * to  :PropertyMapperItem<T2> second map to compare against
 */


sealed class CheckBase<T: Any>(
    val checkName: String,
    val component: Identifiable,
    val sourceKey: ValueBased,
    val validatable: ValidationSubject<T>,
){

    internal var errorMessage: String = ""
    var validatorType: MappingValidator.MappedPropertyValidator = MappingValidator.MappedPropertyValidator.NON_NULLABLE
        internal set

    protected val ignoreMappingList:  MutableList<MappingCheckRecord> = mutableListOf()

    private var mappings: List<MappingCheckRecord> = listOf()
    protected val processableMappings: List<MappingCheckRecord>
        get(){
            val result = mappings.filter { !ignoreMappingList.contains(it) }
            return result
        }

    fun setIgnoredMappingRecord(record: MappingCheckRecord){
        ignoreMappingList.add(record)
    }

    fun setMappings(mappings: List<MappingCheckRecord>){
        this.mappings = mappings
    }

}

class MappingCheck<T: Any>(
    checkName: String,
    component: Identifiable,
    sourceKey: ValueBased,
    validatable: ValidationClass<T>,
):CheckBase<T>(checkName, component, sourceKey, validatable){


    var initialized: Boolean = false


    internal var sequentialBySource: (MappingCheck<T>.(sourceRecord: MappingCheckRecord, testableData: ValidationClass<T>) -> ReportRecordDepr)? =
        null
    internal var bulkPredicate: (MappingCheck<T>.(testable: List<MappingCheckRecord>, toMapping: ValidationClass<T>) -> ReportRecordDepr)? =
        null
    internal var sequentialByValidatable: (MappingCheck<T>.(validatable: ValidationRecord, sourceRecord: List<MappingCheckRecord>) -> ReportRecordDepr)? =
        null

    private fun processSequentialBySource(
        failureMessage: String = "",
        predicate: MappingCheck<T>.(sourceRecord: MappingCheckRecord, testableData: ValidationClass<T>) -> ReportRecordDepr
    ): List<ReportRecordDepr> {
        val report: MutableList<ReportRecordDepr> = mutableListOf()
        processableMappings.forEach { record ->
            val result = predicate.invoke(this, record, validatable as ValidationClass<T>)
            report.add(result)
        }
        return report
    }

    private fun processSequentialByValidatable(
        failureMessage: String = "",
        predicate: MappingCheck<T>.(testableData: ValidationRecord, sourceRecord: List<MappingCheckRecord>) -> ReportRecordDepr
    ): List<ReportRecordDepr> {
        val report: MutableList<ReportRecordDepr> = mutableListOf()
        (validatable as ValidationClass<T>).validatableRecords.forEach {validatableRecord->
            val result = predicate.invoke(this, validatableRecord, processableMappings)
            report.add(result)
        }
        return report
    }


    private fun processBulkValidator(
        failureMessage: String = "",
        sourceList: List<MappingCheckRecord>,
        predicate: MappingCheck<T>.(testable: List<MappingCheckRecord>, toMapping: ValidationClass<T>) -> ReportRecordDepr
    ): List<ReportRecordDepr> {
        val report: MutableList<ReportRecordDepr> = mutableListOf()
        val result = predicate.invoke(this, sourceList, validatable as ValidationClass<T>)
        report.add(result)
        return report
    }



   fun runCheck(
    ): List<ReportRecordDepr> {
        return when (validatorType) {
            MappingValidator.MappedPropertyValidator.NON_NULLABLE,
            MappingValidator.MappedPropertyValidator.PARENT_SET -> {
                processSequentialBySource(errorMessage, sequentialBySource!!)
            }
            MappingValidator.MappedPropertyValidator.FOREIGN_SET -> {
                processSequentialByValidatable(errorMessage, sequentialByValidatable!!)
            }
        }
    }
}

class InstancedCheck<T: Any>(
    checkName: String,
    component: Identifiable,
    sourceKey : ValueBased,
    validatable: ValidationInstance<T>,
): CheckBase<T>(checkName, component, sourceKey, validatable){


    private val ignoreList: MutableList<InstanceRecord<T>> = mutableListOf()
    internal var sequentialByInstance : (InstancedCheck<T>.(validatable: InstanceRecord<T>,  sourceRecord: List<MappingCheckRecord>)-> ReportRecordDepr)? = null

    private fun processSequentialByInstance(
        failureMessage: String = "",
        predicate: InstancedCheck<T>.(testableData: InstanceRecord<T>, sourceRecord: List<MappingCheckRecord>) -> ReportRecordDepr
    ): List<ReportRecordDepr> {
        val report: MutableList<ReportRecordDepr> = mutableListOf()
        (validatable as ValidationInstance<T>).validatableRecords.forEach {record->
            val result = predicate.invoke(this, record, processableMappings)
            report.add(result)
        }
        return report
    }

    fun runCheck(): List<ReportRecordDepr> {
        return when (validatorType) {
            MappingValidator.MappedPropertyValidator.FOREIGN_SET -> {
                processSequentialByInstance(errorMessage, sequentialByInstance!!)
            }
            else -> {
                throw ManagedException("Not supported")
            }
        }
    }

    fun setIgnoredValidationRecord(record: InstanceRecord<T>){
        ignoreList.add(record)
    }


}