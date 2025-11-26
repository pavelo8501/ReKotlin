package po.misc.exceptions.stack_trace

import po.misc.data.PrettyPrint
import po.misc.data.pretty_print.grid.PrettyGrid
import po.misc.data.pretty_print.grid.buildPrettyGrid
import po.misc.data.pretty_print.parts.Orientation
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.data.pretty_print.presets.PrettyPresets
import po.misc.data.pretty_print.presets.RowPresets
import po.misc.data.pretty_print.rows.CellContainer
import po.misc.data.pretty_print.rows.PrettyRow
import po.misc.data.pretty_print.rows.buildPrettyRow
import po.misc.exceptions.TraceOptions


data class CallSiteReport(
    val callerTraceMeta: StackFrameMeta,
    val registrationTraceMeta: StackFrameMeta
): PrettyPrint {

    val reportType:  TraceOptions.TraceType = TraceOptions.TraceType.CallSite


    val callSiteReport: PrettyGrid<CallSiteReport> = buildPrettyGrid{
        buildRow {
            addCell("Call site trace report", PrettyPresets.Header)
        }
        buildRow {
            addCell("Caller trace snapshot", PrettyPresets.Info)
        }
        buildRow(CallSiteReport::callerTraceMeta, RowPresets.VerticalRow){
            addCell(callerTraceMeta::methodName)
            addCell(callerTraceMeta::lineNumber)
            addCell(callerTraceMeta::simpleClassName)
            addCell(callerTraceMeta::consoleLink)
        }
        buildRow {
            addCell("Registration place snapshot", PrettyPresets.Info)
        }
        buildRow(CallSiteReport::registrationTraceMeta, RowPresets.VerticalRow){
            addCell(registrationTraceMeta::methodName)
            addCell(registrationTraceMeta::lineNumber)
            addCell(registrationTraceMeta::simpleClassName)
            addCell(registrationTraceMeta::consoleLink)
        }
    }

    override val formattedString: String get() = callSiteReport.render(this)
}