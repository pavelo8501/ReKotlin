package po.misc.validators.general.reports

import po.misc.context.CTX
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.companion.PrintableCompanion
import po.misc.data.printable.companion.PrintableTemplate
import po.misc.data.printable.companion.nextLine
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.misc.data.templates.matchTemplate
import po.misc.data.templates.templateRule
import po.misc.validators.general.models.CheckStatus
import kotlin.collections.forEach


data class ValidationReport(
    val producer: CTX,
    private  var validationName: String,
): PrintableBase<ValidationReport>(this) {

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

    companion object: PrintableCompanion<ValidationReport>({ValidationReport::class}){

        val Header = createTemplate {
            nextLine {
                "Validating $validationName".colorize(Colour.BLUE)
            }
        }
        val Footer = createTemplate {
            nextLine {
                """Overall Result: ${overallResult.name.matchTemplate(
                    templateRule(overallResult.name.colorize(Colour.GREEN)){overallResult == CheckStatus.PASSED},
                    templateRule(overallResult.name.colorize(Colour.RED)){overallResult == CheckStatus.FAILED},
                    templateRule(overallResult.name.colorize(Colour.YELLOW)){overallResult == CheckStatus.WARNING}
                )}
            """.trimMargin().colorize(Colour.BLUE)
            }
        }
    }
}