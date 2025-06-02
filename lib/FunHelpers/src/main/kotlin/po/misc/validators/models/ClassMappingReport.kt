package po.misc.validators.models

import po.misc.interfaces.Identifiable
import po.misc.interfaces.ValueBased
import po.misc.reflection.properties.mappers.models.PropertyRecord

data class ClassMappingReport(
    val component: Identifiable,
    val dataSourceKey: ValueBased,
    val tester: ValidationClass<*>?,
) {
    var results: List<ReportRecord> = listOf()
    val overallResult : CheckStatus
        get(){
        val isFailed  = results.any { it.status == CheckStatus.FAILED }
        if(isFailed){
            return CheckStatus.FAILED
        }
        return CheckStatus.PASSED
    }

    fun hasFailures(): Boolean = results.any { it.status == CheckStatus.FAILED }
    fun printReport(): String = buildString {
        appendLine("Mapping Report [${component}] $dataSourceKey → $tester")
        results.forEach { r ->
            appendLine(" - ${r.sourcePropertyName}: ${r.status} → ${r.message}")
        }
    }

    fun provideResult(records: List<ReportRecord>):ClassMappingReport{
        results = records
        return this
    }

    companion object{
        fun createReport(checkItem : MappingCheckV2<*>, records: List<ReportRecord>):ClassMappingReport{
           return ClassMappingReport(checkItem.component, checkItem.fromKey, checkItem.validatable).provideResult(records)
        }
        fun createReport(checkItem : InstancedCheckV2<*>, records: List<ReportRecord>):ClassMappingReport{
            return ClassMappingReport(checkItem.component, checkItem.fromKey, null).provideResult(records)
        }
    }
}


class ReportRecord internal constructor(
    val sourcePropertyRecord: PropertyRecord<*>?,
    val sourceColumnName: String,

    val targetPropertyRecord: PropertyRecord<*>?,
){
    var status :CheckStatus = CheckStatus.IDLE
    var message: String = ""

    val sourcePropertyName: String get() = sourcePropertyRecord?.propertyName?:""
    val targetPropertyName: String get() = targetPropertyRecord?.propertyName?:""


    fun setSuccess(message: String? = null):ReportRecord{
        message?.let { this.message = it }
        status = CheckStatus.PASSED
        return this
    }

    fun setFailure(message: String):ReportRecord{
        this.message = message
        status = CheckStatus.FAILED
        return this
    }

    fun setWarning(message: String):ReportRecord{
        this.message = message
        status = CheckStatus.WARNING
        return this
    }
}

