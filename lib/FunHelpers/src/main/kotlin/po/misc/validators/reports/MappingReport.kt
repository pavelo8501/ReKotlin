package po.misc.validators.reports

import po.misc.data.console.Colour
import po.misc.data.console.PrintableBase
import po.misc.data.console.PrintableTemplate
import po.misc.interfaces.ValueBased
import po.misc.validators.models.CheckBase
import po.misc.validators.models.CheckStatus
import po.misc.validators.models.ValidationSubject

data class MappingReport(
    val checkType: CheckBase<*>,
    val dataSourceKey: ValueBased,
    val tester: ValidationSubject<*>?,
): PrintableBase<MappingReport>() {

    override val self: MappingReport = this

    var results: List<ReportRecord> = listOf()
    val overallResult : CheckStatus
        get(){
        val isFailed  = results.any { it.status == CheckStatus.FAILED }
        if(isFailed){
            return CheckStatus.FAILED
        }
        return CheckStatus.PASSED
    }
    val hasFailures: Boolean get() = results.any { it.status == CheckStatus.FAILED }

    fun printReport(): String = buildString {
        print(Header)
        results.forEach { records-> records.print(ReportRecord.GeneralTemplate) }
        print(Footer)
    }

    fun provideResult(records: List<ReportRecord>):MappingReport{
        results = records
        return this
    }

    companion object{

        val Header : PrintableTemplate<MappingReport> = PrintableTemplate(1){
            "Validating ${checkType.checkName} [${checkType.component.completeName}]" colourOf Colour.BLUE
        }

        val Footer : PrintableTemplate<MappingReport> = PrintableTemplate(2){
            """Overall Result: ${overallResult.toString().makeOfColour(
                colourRule(Colour.GREEN){ overallResult == CheckStatus.PASSED },
                colourRule(Colour.RED){ overallResult == CheckStatus.FAILED },
                colourRule(Colour.YELLOW){ overallResult == CheckStatus.WARNING }
            )}
            """.trimMargin() colourOf Colour.BLUE
        }

        fun createReport(checkItem : CheckBase<*>, records: List<ReportRecord>):MappingReport{
           return MappingReport(checkItem, checkItem.sourceKey, checkItem.validatable).provideResult(records)
        }
    }
}

