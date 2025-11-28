package po.misc.exceptions.stack_trace

import po.misc.data.PrettyPrint
import po.misc.data.pretty_print.grid.PrettyGrid
import po.misc.data.pretty_print.grid.buildPrettyGrid
import po.misc.data.pretty_print.presets.PrettyPresets
import po.misc.data.pretty_print.presets.RowPresets
import po.misc.debugging.stack_tracer.StackFrameMeta
import po.misc.exceptions.TraceOptions


data class CallSiteReport(
    val callerTraceMeta: StackFrameMeta,
    val registrationTraceMeta: StackFrameMeta,
    var hopFrames: List<StackFrameMeta> = emptyList()
): PrettyPrint {

    val reportType:  TraceOptions.TraceType = TraceOptions.TraceType.CallSite

    val callSiteReportOld: PrettyGrid<CallSiteReport> = buildPrettyGrid{
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

        if(hopFrames.isNotEmpty()){
            buildRow {
                addCell("Registered hops", PrettyPresets.Info)
            }
            buildRows(::hopFrames, RowPresets.VerticalRow){
                addCell(StackFrameMeta::methodName)
                addCell(StackFrameMeta::lineNumber)
                addCell(StackFrameMeta::simpleClassName)
                addCell(StackFrameMeta::consoleLink)
            }
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

    val report: PrettyGrid<CallSiteReport> = buildPrettyGrid{
        buildRow {
            addCell("Call site trace report", PrettyPresets.Header)
        }
        buildRow {
            addCell("Caller trace snapshot", PrettyPresets.Info)
        }
        useTemplate(StackFrameMeta.frameMetaTemplate){
            callerTraceMeta
        }
        buildRow {
            addCell("Registered hops", PrettyPresets.Info)
        }
        useTemplateForList(StackFrameMeta.frameMetaTemplate){
            hopFrames
        }
        buildRow {
            addCell("Registration place snapshot", PrettyPresets.Info)
        }
        useTemplate(StackFrameMeta.frameMetaTemplate){
            registrationTraceMeta
        }
    }
    override val formattedString: String get() = report.render(this)
}