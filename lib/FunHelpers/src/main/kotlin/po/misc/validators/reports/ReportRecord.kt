package po.misc.validators.reports

import po.misc.data.console.Colour
import po.misc.data.console.PrintableBase
import po.misc.data.console.PrintableTemplate
import po.misc.validators.models.CheckStatus


class ReportRecord internal constructor(
    val sourceName: String,
    val targetName: String,
): PrintableBase<ReportRecord>(){

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
       val GeneralTemplate : PrintableTemplate<ReportRecord> = PrintableTemplate(1){
           "Check Source:$sourceName Target:$targetName Status:${status.toString().makeOfColour(
               colourRule(Colour.GREEN){status == CheckStatus.PASSED},
               colourRule(Colour.RED){status == CheckStatus.FAILED},
               colourRule(Colour.YELLOW){status == CheckStatus.WARNING}
           )} ${message.toString()} ".withIndention(4, "-")
       }
    }
}