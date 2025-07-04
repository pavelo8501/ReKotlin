package po.misc.validators.general.reports

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
import po.misc.validators.general.models.CheckStatus
import kotlin.collections.forEach


data class ValidationReport(
   private  var validationName: String,
): PrintableBase<ValidationReport>(Header) {

    override val itemId : ValueBased = toValueBased(0)
    override val emitter: Identifiable = asIdentifiable("ValidationReport", "ValidationReport")

    override val self: ValidationReport = this

    init {
        addTemplate(Header, Footer)
    }

    fun setName(name: String){
        validationName = name
    }

    fun addRecord(record:ReportRecord): ReportRecord{
        addChild(record)
        return record
    }

    fun getRecords (): List<ReportRecord>{
       return children.filterIsInstance<ReportRecord>()
    }


    val overallResult : CheckStatus
        get(){
            val isFailed  =  getRecords().any { it.result == CheckStatus.FAILED }
            if(isFailed){
                return CheckStatus.FAILED
            }
            return CheckStatus.PASSED
        }


    val hasFailures: Boolean get() = getRecords().any { it.result == CheckStatus.FAILED }

    fun printReport(): String = buildString {
        super.echo(Header)
        getRecords().forEach {  record-> record.echo(ReportRecord.GeneralTemplate)}
        super.echo(Footer)
    }

    companion object{

        val Header : PrintableTemplate<ValidationReport> = PrintableTemplate("Header"){
            "Validating $validationName".colorize(Colour.BLUE)
        }

        val Footer : PrintableTemplate<ValidationReport> = PrintableTemplate("Footer"){
            """Overall Result: ${overallResult.name.matchTemplate(
                templateRule(overallResult.name.colorize(Colour.GREEN)){overallResult == CheckStatus.PASSED},
                templateRule(overallResult.name.colorize(Colour.RED)){overallResult == CheckStatus.FAILED},
                templateRule(overallResult.name.colorize(Colour.YELLOW)){overallResult == CheckStatus.WARNING}
            )}
            """.trimMargin().colorize(Colour.BLUE)
        }
    }
}