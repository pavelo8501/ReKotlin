package po.misc.validators

import po.misc.interfaces.ValueBased
import po.misc.reflection.properties.mappers.interfaces.MappablePropertyRecord
import po.misc.reflection.properties.mappers.models.PropertyMapperRecord
import po.misc.validators.helpers.reportWarning
import po.misc.validators.models.ClassMappingReport
import po.misc.validators.models.InstancedCheckV2
import po.misc.validators.models.MappingCheckRecord
import po.misc.validators.models.MappingCheckV2

class MappingValidator() {
    enum class MappedPropertyValidator(override val value: Int) : ValueBased {
        NON_NULLABLE(1),
        PARENT_SET(2),
        FOREIGN_SET(3)
    }

    private val reportList: MutableList<ClassMappingReport> = mutableListOf()

    fun <T : Any> executeCheck(
        mappingCheck: MappingCheckV2<T>,
        sourceRecord: List<MappingCheckRecord>
    ): ClassMappingReport {
        val checked = if (mappingCheck.validatable.records.isEmpty()) {
            mappingCheck.reportWarning("Validatable list is empty", sourceRecord)
        } else {
            mappingCheck.runCheck(sourceRecord)
        }
        val report = ClassMappingReport.createReport(mappingCheck, checked)
        reportList.add(report)
        return report
    }

    fun <T : Any> executeCheck(
        mappingCheck: InstancedCheckV2<T>,
        sourceRecord: List<MappingCheckRecord>
    ): ClassMappingReport {
        val checked = if (mappingCheck.instances.isEmpty()) {
            mappingCheck.reportWarning("Validatable list is empty", sourceRecord)
        } else {
            mappingCheck.runCheck(sourceRecord)
        }
        val report = ClassMappingReport.createReport(mappingCheck, checked)
        reportList.add(report)
        return report
    }
}