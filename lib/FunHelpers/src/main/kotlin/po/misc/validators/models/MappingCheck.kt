package po.misc.validators.models

import po.misc.exceptions.ManagedException
import po.misc.interfaces.Identifiable
import po.misc.interfaces.ValueBased
import po.misc.reflection.properties.mappers.interfaces.MappablePropertyRecord
import po.misc.validators.MappingValidator
import po.misc.validators.models.MappingCheckRecord
import po.misc.validators.models.ValidationInstance
import kotlin.collections.List


/***
 * component: Identifiable To identify  who supplied data for checking
 * from  : PropertyMapperItem<T1> property map to be used
 * to  :PropertyMapperItem<T2> second map to compare against
 */
//open  class MappingCheck<SOURCE: MappablePropertyRecord<*>>(
//    val component: Identifiable,
//    val fromKey: ValueBased,
//    val validatable: ValidationClass<*>,
//    private val validator : MappingValidator<SOURCE>
//) {
//
//    var validatorType: MappingValidator.MappedPropertyValidator = MappingValidator.MappedPropertyValidator.NON_NULLABLE
//        internal set
//
//    var initialized: Boolean = false
//    internal var errorMessage: String = ""
//
//    internal var sequentialBySource: (MappingCheck<SOURCE>.(sourceRecord: MappingCheckRecord, testableData: ValidationClass<*>) -> ReportRecord)? =
//        null
//    internal var bulkPredicate: (MappingCheck<SOURCE>.(testable: List<MappingCheckRecord>, toMapping: ValidationClass<*>) -> ReportRecord)? =
//        null
//    internal var sequentialByValidatable: (MappingCheck<SOURCE>.(testableData: ValidationRecord, sourceRecord: List<MappingCheckRecord>) -> ReportRecord)? =
//        null
//
//    private fun processSequentialBySource(
//        failureMessage: String = "",
//        sourceList: List<MappingCheckRecord>,
//        predicate: MappingCheck<SOURCE>.(sourceRecord: MappingCheckRecord, testableData: ValidationClass<*>) -> ReportRecord
//    ): List<ReportRecord> {
//        val report: MutableList<ReportRecord> = mutableListOf()
//        sourceList.forEach { record ->
//            val result = predicate.invoke(this, record, validatable)
//            report.add(result)
//        }
//        return report
//    }
//
//    private fun processSequentialByValidatable(
//        failureMessage: String = "",
//        sourceList: List<MappingCheckRecord>,
//        predicate: MappingCheck<SOURCE>.(testableData: ValidationRecord, sourceRecord: List<MappingCheckRecord>) -> ReportRecord
//    ): List<ReportRecord> {
//        val report: MutableList<ReportRecord> = mutableListOf()
//
//        validatable.records.forEach {validatableRecord->
//            val result = predicate.invoke(this, validatableRecord, sourceList)
//            report.add(result)
//        }
//        return report
//    }
//
//
//    private fun processBulkValidator(
//        failureMessage: String = "",
//        sourceList: List<MappingCheckRecord>,
//        predicate: MappingCheck<SOURCE>.(testable: List<MappingCheckRecord>, toMapping: ValidationClass<*>) -> ReportRecord
//    ): List<ReportRecord> {
//        val report: MutableList<ReportRecord> = mutableListOf()
//        val result = predicate.invoke(this, sourceList, validatable)
//        report.add(result)
//        return report
//    }
//
//    fun runCheck(
//        sourceList: List<MappingCheckRecord>,
//    ): List<ReportRecord> {
//        return when (validatorType) {
//            MappingValidator.MappedPropertyValidator.NON_NULLABLE,
//            MappingValidator.MappedPropertyValidator.PARENT_SET -> {
//                processSequentialBySource(errorMessage, sourceList, sequentialBySource!!)
//            }
//            MappingValidator.MappedPropertyValidator.FOREIGN_SET -> {
//                processSequentialByValidatable(errorMessage, sourceList, sequentialByValidatable!!)
//            }
//        }
//    }
//}

class MappingCheckV2<T: Any>(
    val component: Identifiable,
    val fromKey: ValueBased,
    val validatable: ValidationClass<T>,
    val validator : MappingValidator
){

    var validatorType: MappingValidator.MappedPropertyValidator = MappingValidator.MappedPropertyValidator.NON_NULLABLE
        internal set

    var initialized: Boolean = false
    internal var errorMessage: String = ""

    internal var sequentialBySource: (MappingCheckV2<T>.(sourceRecord: MappingCheckRecord, testableData: ValidationClass<T>) -> ReportRecord)? =
        null
    internal var bulkPredicate: (MappingCheckV2<T>.(testable: List<MappingCheckRecord>, toMapping: ValidationClass<T>) -> ReportRecord)? =
        null
    internal var sequentialByValidatable: (MappingCheckV2<T>.(validatable: ValidationRecord, sourceRecord: List<MappingCheckRecord>) -> ReportRecord)? =
        null

    private fun processSequentialBySource(
        failureMessage: String = "",
        sourceList: List<MappingCheckRecord>,
        predicate: MappingCheckV2<T>.(sourceRecord: MappingCheckRecord, testableData: ValidationClass<T>) -> ReportRecord
    ): List<ReportRecord> {
        val report: MutableList<ReportRecord> = mutableListOf()
        sourceList.forEach { record ->
            val result = predicate.invoke(this, record, validatable)
            report.add(result)
        }
        return report
    }

    private fun processSequentialByValidatable(
        failureMessage: String = "",
        sourceList: List<MappingCheckRecord>,
        predicate: MappingCheckV2<T>.(testableData: ValidationRecord, sourceRecord: List<MappingCheckRecord>) -> ReportRecord
    ): List<ReportRecord> {
        val report: MutableList<ReportRecord> = mutableListOf()

        validatable.records.forEach {validatableRecord->
            val result = predicate.invoke(this, validatableRecord, sourceList)
            report.add(result)
        }
        return report
    }


    private fun processBulkValidator(
        failureMessage: String = "",
        sourceList: List<MappingCheckRecord>,
        predicate: MappingCheckV2<T>.(testable: List<MappingCheckRecord>, toMapping: ValidationClass<T>) -> ReportRecord
    ): List<ReportRecord> {
        val report: MutableList<ReportRecord> = mutableListOf()
        val result = predicate.invoke(this, sourceList, validatable)
        report.add(result)
        return report
    }

    fun runCheck(
        sourceList: List<MappingCheckRecord>,
    ): List<ReportRecord> {
        return when (validatorType) {
            MappingValidator.MappedPropertyValidator.NON_NULLABLE,
            MappingValidator.MappedPropertyValidator.PARENT_SET -> {
                processSequentialBySource(errorMessage, sourceList, sequentialBySource!!)
            }
            MappingValidator.MappedPropertyValidator.FOREIGN_SET -> {
                processSequentialByValidatable(errorMessage, sourceList, sequentialByValidatable!!)
            }
        }
    }

}

class InstancedCheckV2<T: Any>(
    val component: Identifiable,
    val fromKey: ValueBased,
    val instances: List<InstanceRecord<T>>,
    val validator : MappingValidator
){

    var validatorType: MappingValidator.MappedPropertyValidator = MappingValidator.MappedPropertyValidator.NON_NULLABLE
        internal set

    var initialized: Boolean = false
    internal var errorMessage: String = ""


    internal var sequentialByInstance : (InstancedCheckV2<T>.(validatable: InstanceRecord<T>,  sourceRecord: List<MappingCheckRecord>)-> ReportRecord)? = null

    fun runCheck(
        sourceList: List<MappingCheckRecord>,
    ): List<ReportRecord> {
        return when (validatorType) {
            MappingValidator.MappedPropertyValidator.FOREIGN_SET -> {
                processSequentialByInstance(errorMessage, sourceList, sequentialByInstance!!)
            }
            else -> {
                throw ManagedException("Not supported")
            }
        }
    }

    private fun processSequentialByInstance(
        failureMessage: String = "",
        sourceList: List<MappingCheckRecord>,
        predicate: InstancedCheckV2<T>.(testableData: InstanceRecord<T>, sourceRecord: List<MappingCheckRecord>) -> ReportRecord
    ): List<ReportRecord> {
        val report: MutableList<ReportRecord> = mutableListOf()

        instances.forEach {validatableRecord->
            val result = predicate.invoke(this, validatableRecord, sourceList)
            report.add(result)
        }

        return report
    }


}