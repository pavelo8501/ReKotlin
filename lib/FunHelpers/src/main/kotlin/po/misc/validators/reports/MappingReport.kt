package po.misc.validators.reports

import po.misc.data.PrintableBase
import po.misc.data.console.PrintableTemplate
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.misc.data.templates.matchTemplate
import po.misc.data.templates.templateRule
import po.misc.interfaces.Identifiable
import po.misc.interfaces.ValueBased
import po.misc.interfaces.asIdentifiable
import po.misc.interfaces.toValueBased
import po.misc.validators.models.CheckBase
import po.misc.validators.models.CheckStatus
import po.misc.validators.models.ValidationSubject

data class MappingReport(
    val checkType: CheckBase<*>,
    val dataSourceKey: ValueBased,
    val tester: ValidationSubject<*>?,
): PrintableBase<MappingReport>() {

    override val itemId : ValueBased = dataSourceKey
    override val emitter: Identifiable = checkType.component

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
        results.forEach { records-> records.printTemplate(ReportRecord.GeneralTemplate) }
        print(Footer)
    }

    fun provideResult(records: List<ReportRecord>):MappingReport{
        results = records
        return this
    }

    companion object{

        val Header : PrintableTemplate<MappingReport> = PrintableTemplate(){
            "Validating ${checkType.checkName} [${checkType.component.completeName}]".colorize(Colour.BLUE)
        }

        val Footer : PrintableTemplate<MappingReport> = PrintableTemplate{
            """Overall Result: ${overallResult.matchTemplate(
                templateRule(toString().colorize(Colour.GREEN)){overallResult == CheckStatus.PASSED},
                templateRule(toString().colorize(Colour.RED)){overallResult == CheckStatus.FAILED},
                templateRule(toString().colorize(Colour.YELLOW)){overallResult == CheckStatus.WARNING}
            )}
            """.trimMargin().colorize(Colour.BLUE)
        }

        fun createReport(checkItem : CheckBase<*>, records: List<ReportRecord>):MappingReport{
           return MappingReport(checkItem, checkItem.sourceKey, checkItem.validatable).provideResult(records)
        }
    }
}

