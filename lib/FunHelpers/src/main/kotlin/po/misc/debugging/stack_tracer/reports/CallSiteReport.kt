package po.misc.debugging.stack_tracer.reports

import po.misc.data.PrettyPrint
import po.misc.data.pretty_print.PrettyGrid
import po.misc.data.pretty_print.grid.buildPrettyGrid
import po.misc.data.pretty_print.parts.CellPresets
import po.misc.data.pretty_print.parts.Orientation
import po.misc.debugging.stack_tracer.StackFrameMeta
import po.misc.debugging.stack_tracer.StackFrameMeta.Template
import po.misc.debugging.stack_tracer.TraceOptions

data class CallSiteReport(
    val callerFrame: StackFrameMeta,
    val registrationFrame: StackFrameMeta,
    var hopFrames: List<StackFrameMeta> = emptyList(),
    val reportType:  TraceOptions.TraceType = TraceOptions.TraceType.CallSite
): PrettyPrint {

    override val formattedString: String get() = callSiteTemplate.render(this)

    companion object {

        val callSiteTemplate: PrettyGrid<CallSiteReport> = buildPrettyGrid(TraceOptions.TraceType.CallSite) {
            headedRow("Call site trace report")
            buildRow{
                add("Caller trace snapshot", CellPresets.Info)
            }
            useTemplate(StackFrameMeta.frameTemplate, CallSiteReport::callerFrame)
            headedRow("Registered hops")
            useTemplate(StackFrameMeta.frameTemplate, CallSiteReport::hopFrames){
                 orientation = Orientation.Horizontal
                 exclude(Template.ConsoleLink)
            }
            buildRow {
                add("Registration place snapshot", CellPresets.Info)
            }
            useTemplate(StackFrameMeta.frameTemplate, CallSiteReport::registrationFrame)
        }
    }
}