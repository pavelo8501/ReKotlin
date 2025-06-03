package po.misc.validators

import po.misc.interfaces.ValueBased
import po.misc.reflection.properties.mappers.models.PropertyRecord
import po.misc.validators.helpers.reportWarning
import po.misc.validators.models.CheckBase
import po.misc.validators.models.InstancedCheck
import po.misc.validators.models.MappingCheck
import po.misc.validators.models.MappingCheckRecord
import po.misc.validators.models.ValidationInstance
import po.misc.validators.reports.MappingReport

class MappingValidator() {
    enum class MappedPropertyValidator(override val value: Int) : ValueBased {
        NON_NULLABLE(1),
        PARENT_SET(2),
        FOREIGN_SET(3)
    }

    private val reportList: MutableList<MappingReport> = mutableListOf()

    private val ignoreProperties : MutableList<(PropertyRecord<*>)-> Boolean> = mutableListOf()

    private fun assignIgnoredMapping(mappingCheck: CheckBase<*>, sourceRecord: List<MappingCheckRecord>){
        sourceRecord.forEach { source ->
            ignoreProperties.forEach { condition ->
                source.propertyRecord?.let {
                    val shouldIgnore = condition.invoke(it)
                    if(shouldIgnore){
                        mappingCheck.setIgnoredMappingRecord(source)
                    }
                }
            }
        }
    }

    fun <T : Any> executeCheck(
        mappingCheck: CheckBase<T>,
        sourceRecord: List<MappingCheckRecord>
    ): MappingReport {

        assignIgnoredMapping(mappingCheck, sourceRecord)

        val checked = when(mappingCheck){
            is MappingCheck<T> -> {
                if (mappingCheck.validatable.validatableRecords.isEmpty()) {
                    mappingCheck.reportWarning("Validatable list is empty", sourceRecord)
                } else {
//                    mappingCheck.validatable.validatableRecords.forEach {record->
//                        ignoreProperties.forEach {condition->
//                            val shouldIgnore = condition.invoke(record.propertyRecord)
//                            if(shouldIgnore){
//                                mappingCheck.setIgnoredMappingRecord(record)
//                            }
//                        }
//                    }
                    mappingCheck.runCheck()
                }
            }
            is InstancedCheck<T>->{
                if (mappingCheck.validatable.validatableRecords.isEmpty()) {
                    mappingCheck.reportWarning("Validatable list is empty", sourceRecord)
                } else {
//                    mappingCheck.validatable.validatableRecords.forEach { record ->
//                        ignoreProperties.forEach { condition ->
//                            record.propertyRecord?.let {
//                                val shouldIgnore = condition.invoke(it)
//                                if(shouldIgnore){
//                                    mappingCheck.setIgnoredValidationRecord(record)
//                                }
//                            }
//                        }
//                    }
                    mappingCheck.runCheck()
                }
            }
        }
        val report = MappingReport.Companion.createReport(mappingCheck, checked)
        report.printReport()
        reportList.add(report)
        return report
    }

    fun ignoreProperty(ignoreCondition:  (PropertyRecord<*>)-> Boolean):MappingValidator{
        ignoreProperties.add(ignoreCondition)
        return this
    }

//    fun <T : Any> executeCheck(
//        mappingCheck: InstancedCheck<T>,
//        sourceRecord: List<MappingCheckRecord>
//    ): MappingReport {
//
//
//
//        val checked = if (mappingCheck.validatable. .isEmpty()) {
//            mappingCheck.reportWarning("Validatable list is empty", sourceRecord)
//        } else {
//            mappingCheck.runCheck(sourceRecord)
//        }
//        val report = MappingReport.Companion.createReport(mappingCheck, checked)
//        reportList.add(report)
//        return report
//    }
}