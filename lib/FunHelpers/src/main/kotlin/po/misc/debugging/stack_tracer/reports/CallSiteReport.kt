package po.misc.debugging.stack_tracer.reports

import po.misc.data.PrettyPrint
import po.misc.data.pretty_print.grid.PrettyGrid
import po.misc.data.pretty_print.grid.addHeadedRow
import po.misc.data.pretty_print.grid.buildPrettyGrid
import po.misc.data.pretty_print.grid.buildRow
import po.misc.data.pretty_print.parts.CellPresets
import po.misc.data.pretty_print.parts.Orientation
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.debugging.stack_tracer.StackFrameMeta
import po.misc.debugging.stack_tracer.TraceOptions

data class CallSiteReport(
    val callerTraceMeta: StackFrameMeta,
    val registrationTraceMeta: StackFrameMeta,
    var hopFrames: List<StackFrameMeta> = emptyList(),
    val reportType:  TraceOptions.TraceType = TraceOptions.TraceType.CallSite
): PrettyPrint {

    override val formattedString: String get() = callSiteReport.render(this)

    companion object {

        val callSiteReport: PrettyGrid<CallSiteReport> = buildPrettyGrid(TraceOptions.TraceType.CallSite) {
            addHeadedRow("Call site trace report")

            buildRow{
               addCell("Caller trace snapshot", CellPresets.Info)
            }
            useTemplate(StackFrameMeta.frameTemplate, CallSiteReport::callerTraceMeta)

            addHeadedRow("Registered hops")
            useListTemplate(StackFrameMeta.frameTemplate, CallSiteReport::hopFrames, Orientation.Horizontal)

            buildRow {
                addCell("Registration place snapshot", CellPresets.Info)
            }
            useTemplate(StackFrameMeta.frameTemplate, CallSiteReport::registrationTraceMeta)
        }
    }
}