package po.misc.validators.general.reports

import po.misc.context.CTX
import po.misc.data.printable.PrintableBase
import po.misc.data.console.PrintableTemplate
import po.misc.data.helpers.emptyOnNull
import po.misc.data.helpers.withIndention
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.misc.data.templates.matchTemplate
import po.misc.data.templates.templateRule
import po.misc.validators.general.ValidationContainerBase
import po.misc.validators.general.models.CheckStatus


class ReportRecord internal constructor(
    override val producer: CTX,
    val recordName: String,
    val result: CheckStatus,
): PrintableBase<ReportRecord>(GeneralTemplate) {


    var message: String? = null
        private set

   // override val itemId: ValueBased = toValueBased(0)


    override val self: ReportRecord = this

    fun setMessage(message: String):ReportRecord{
        this.message = message
        return this
    }

    companion object{

        val GeneralTemplate : PrintableTemplate<ReportRecord> = PrintableTemplate("GeneralTemplate"){
            "Check:$recordName | Status:${result.name.matchTemplate(
                templateRule(result.name.colorize(Colour.GREEN)){ result == CheckStatus.PASSED },
                templateRule(result.name.colorize(Colour.RED)){ result == CheckStatus.FAILED },
                templateRule(result.name.colorize(Colour.YELLOW)){ result == CheckStatus.WARNING }
            )} ${message.emptyOnNull()} ".withIndention(4, "-")
        }



        fun success(container: ValidationContainerBase<*>, checkName: String):ReportRecord{
            return ReportRecord(container.identifiable, checkName, CheckStatus.PASSED)
        }
        fun fail(container: ValidationContainerBase<*>, checkName: String, message: String):ReportRecord{
          return ReportRecord(container.identifiable, checkName, CheckStatus.FAILED).setMessage(message)
        }
        fun fail(container: ValidationContainerBase<*>, checkName: String, th: Throwable):ReportRecord{
            return ReportRecord(container.identifiable, checkName, CheckStatus.FAILED).setMessage(th.message.toString())
        }

    }


}