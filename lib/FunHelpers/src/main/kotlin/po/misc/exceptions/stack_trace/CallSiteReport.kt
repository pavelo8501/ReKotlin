package po.misc.exceptions.stack_trace

import po.misc.data.PrettyPrint
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.data.pretty_print.rows.CellContainer
import po.misc.data.pretty_print.rows.PrettyRow
import po.misc.data.pretty_print.rows.buildPrettyRow
import po.misc.exceptions.TraceOptions


data class CallSiteReport(
    val callerTraceMeta: StackFrameMeta,
    val registrationTraceMeta: StackFrameMeta
): PrettyPrint{

    val reportType:  TraceOptions.TraceType = TraceOptions.TraceType.CallSite
    val callSiteReport: PrettyRow = buildPrettyRow<CallSiteReport>(CellContainer, RowOptions(PrettyRow.Orientation.Vertical)) {

        addCell("Call site trace report")
        addCell("Caller trace snapshot")

        addCell(::callerTraceMeta){
            it.methodName
        }
        addCell("Registration place snapshot")
        addCell(CallSiteReport::registrationTraceMeta){
            it.methodName
        }
    }

    override val formattedString: String get() = callSiteReport.render(this)
}