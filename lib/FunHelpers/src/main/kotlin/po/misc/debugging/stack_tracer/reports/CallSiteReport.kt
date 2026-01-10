package po.misc.debugging.stack_tracer.reports

import po.misc.data.PrettyPrint
import po.misc.data.pretty_print.PrettyGrid
import po.misc.data.pretty_print.buildPrettyGrid
import po.misc.data.pretty_print.parts.options.CellPresets
import po.misc.debugging.stack_tracer.StackFrameMeta

data class CallSiteReport(
    val callerFrame: StackFrameMeta,
    val registrationFrame: StackFrameMeta,
    var hopFrames: List<StackFrameMeta> = emptyList(),

): PrettyPrint {

    override val formattedString: String get() = callSiteTemplate.render(this)

    companion object {

        val callSiteTemplate: PrettyGrid<CallSiteReport> = buildPrettyGrid() {
            buildRow{
                add("Call site trace report")
            }
            buildRow{
                add("Caller trace snapshot", CellPresets.Info)
            }
            useGrid(StackFrameMeta.frameTemplate, CallSiteReport::callerFrame)
            buildRow{
                add("Registered hops")
            }
//            useTemplate(StackFrameMeta.frameTemplate, CallSiteReport::hopFrames){
//                 orientation = Orientation.Horizontal
//            }
            buildRow {
                add("Registration place snapshot", CellPresets.Info)
            }
            useGrid(StackFrameMeta.frameTemplate, CallSiteReport::registrationFrame)
        }
    }
}