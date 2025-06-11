package po.misc.validators.mapping

import po.misc.interfaces.ValueBased
import po.misc.reflection.mappers.models.PropertyRecord
import po.misc.validators.mapping.helpers.reportWarning
import po.misc.validators.mapping.models.CheckBase
import po.misc.validators.mapping.models.InstancedCheck
import po.misc.validators.mapping.models.MappingCheck
import po.misc.validators.mapping.models.MappingCheckRecord
import po.misc.validators.mapping.reports.MappingReportDepr

class MappingValidator() {
    enum class MappedPropertyValidator(override val value: Int) : ValueBased {
        NON_NULLABLE(1),
        PARENT_SET(2),
        FOREIGN_SET(3)
    }

    val reportList: MutableList<MappingReportDepr> = mutableListOf()

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
    ): MappingReportDepr {

        assignIgnoredMapping(mappingCheck, sourceRecord)

        val checked = when(mappingCheck){
            is MappingCheck<T> -> {
                if (mappingCheck.validatable.validatableRecords.isEmpty()) {
                    mappingCheck.reportWarning("Validatable list is empty", sourceRecord)
                } else {
                    mappingCheck.runCheck()
                }
            }
            is InstancedCheck<T> ->{
                if (mappingCheck.validatable.validatableRecords.isEmpty()) {
                    mappingCheck.reportWarning("Validatable list is empty", sourceRecord)
                } else {
                    mappingCheck.runCheck()
                }
            }
        }
        val report = MappingReportDepr.Companion.createReport(mappingCheck, checked)
        report.printReport()
        reportList.add(report)
        return report
    }

    fun ignoreProperty(ignoreCondition:  (PropertyRecord<*>)-> Boolean):MappingValidator{
        ignoreProperties.add(ignoreCondition)
        return this
    }
}