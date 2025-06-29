package po.misc.validators.mapping.reports

import po.misc.data.printable.PrintableBase
import po.misc.data.console.PrintableTemplate
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.misc.data.templates.matchTemplate
import po.misc.data.templates.templateRule
import po.misc.interfaces.Identifiable
import po.misc.interfaces.ValueBased
import po.misc.validators.general.models.CheckStatus
import po.misc.validators.mapping.models.CheckBase
import po.misc.validators.mapping.models.ValidationSubject

data class MappingReportDepr(
    val checkType: CheckBase<*>,
    val dataSourceKey: ValueBased,
    val tester: ValidationSubject<*>?,
): PrintableBase<MappingReportDepr>(Header) {

    override val itemId : ValueBased = dataSourceKey
    override val emitter: Identifiable = checkType.component

    override val self: MappingReportDepr = this

    var results: List<ReportRecordDepr> = listOf()
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
        echo(Header)
        results.forEach { records-> records.echo(ReportRecordDepr.GeneralTemplate) }
        echo(Footer)
    }

    fun provideResult(records: List<ReportRecordDepr>):MappingReportDepr{
        results = records
        return this
    }

    companion object{

        val Header : PrintableTemplate<MappingReportDepr> = PrintableTemplate("Header"){
            "Validating ${checkType.checkName} [${checkType.component.completeName}]".colorize(Colour.BLUE)
        }

        val Footer : PrintableTemplate<MappingReportDepr> = PrintableTemplate("Footer"){
            """Overall Result: ${overallResult.matchTemplate(
                templateRule(toString().colorize(Colour.GREEN)){overallResult == CheckStatus.PASSED},
                templateRule(toString().colorize(Colour.RED)){overallResult == CheckStatus.FAILED},
                templateRule(toString().colorize(Colour.YELLOW)){overallResult == CheckStatus.WARNING}
            )}
            """.trimMargin().colorize(Colour.BLUE)
        }

        fun createReport(checkItem : CheckBase<*>, records: List<ReportRecordDepr>):MappingReportDepr{
           return MappingReportDepr(checkItem, checkItem.sourceKey, checkItem.validatable).provideResult(records)
        }
    }
}

