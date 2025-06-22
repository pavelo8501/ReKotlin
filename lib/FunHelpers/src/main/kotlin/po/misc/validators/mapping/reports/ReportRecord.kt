package po.misc.validators.mapping.reports

import po.misc.data.PrintableBase
import po.misc.data.console.PrintableTemplate
import po.misc.data.helpers.withIndention
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.misc.data.templates.matchTemplate
import po.misc.data.templates.templateRule
import po.misc.interfaces.Identifiable
import po.misc.interfaces.ValueBased
import po.misc.interfaces.asIdentifiable
import po.misc.interfaces.toValueBased
import po.misc.validators.general.models.CheckStatus


class ReportRecordDepr internal constructor(
    val sourceName: String,
    val targetName: String,
): PrintableBase<ReportRecordDepr>(GeneralTemplate){


    override val itemId : ValueBased = toValueBased(0)
    override val emitter: Identifiable = asIdentifiable(sourceName,targetName)

    override val self: ReportRecordDepr = this

    var status : CheckStatus = CheckStatus.WARNING
    var message: String? = null

    fun setSuccess(message: String? = null):ReportRecordDepr{
        message?.let { this.message = it }
        status = CheckStatus.PASSED
        return this
    }

    fun setFailure(message: String):ReportRecordDepr{
        this.message = message
        status = CheckStatus.FAILED
        return this
    }

    fun setWarning(message: String):ReportRecordDepr{
        this.message = message
        status = CheckStatus.WARNING
        return this
    }

    companion object{
       val GeneralTemplate : PrintableTemplate<ReportRecordDepr> = PrintableTemplate("GeneralTemplate"){
           "Check Source:$sourceName Target:$targetName Status:${status.matchTemplate(
               templateRule(toString().colorize(Colour.GREEN)){ status == CheckStatus.PASSED },
               templateRule(toString().colorize(Colour.RED)){ status == CheckStatus.FAILED },
               templateRule(toString().colorize(Colour.YELLOW)){ status == CheckStatus.WARNING }
           )} ${message.toString()} ".withIndention(4, "-")
       }
    }
}