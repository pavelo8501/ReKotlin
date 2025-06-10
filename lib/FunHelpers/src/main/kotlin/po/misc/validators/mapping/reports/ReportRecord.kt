package po.misc.validators.mapping.reports

import po.misc.data.PrintableBase
import po.misc.data.console.PrintableTemplate
import po.misc.data.console.helpers.withIndention
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.misc.data.templates.matchTemplate
import po.misc.data.templates.templateRule
import po.misc.interfaces.Identifiable
import po.misc.interfaces.ValueBased
import po.misc.interfaces.asIdentifiable
import po.misc.interfaces.toValueBased
import po.misc.validators.mapping.models.CheckStatus


class ReportRecord internal constructor(
    val sourceName: String,
    val targetName: String,
): PrintableBase<ReportRecord>(){


    override val itemId : ValueBased = toValueBased(0)
    override val emitter: Identifiable = asIdentifiable(sourceName,targetName)

    override val self: ReportRecord = this

    var status : CheckStatus = CheckStatus.IDLE
    var message: String? = null

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

    companion object{
       val GeneralTemplate : PrintableTemplate<ReportRecord> = PrintableTemplate(){
           "Check Source:$sourceName Target:$targetName Status:${status.matchTemplate(
               templateRule(toString().colorize(Colour.GREEN)){ status == CheckStatus.PASSED },
               templateRule(toString().colorize(Colour.RED)){ status == CheckStatus.FAILED },
               templateRule(toString().colorize(Colour.YELLOW)){ status == CheckStatus.WARNING }
           )} ${message.toString()} ".withIndention(4, "-")
       }
    }
}