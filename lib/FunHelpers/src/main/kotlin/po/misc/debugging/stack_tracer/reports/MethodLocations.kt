package po.misc.debugging.stack_tracer.reports

import po.misc.data.PrettyPrint
import po.misc.data.pretty_print.PrettyGrid
import po.misc.data.pretty_print.grid.buildPrettyGrid
import po.misc.debugging.stack_tracer.StackFrameMeta

data class MethodLocations(
    val header:String,
    val methodName:String,
    val frames: List<StackFrameMeta>
): PrettyPrint {


    override val formattedString: String
        get() = ""

    companion object{

        val template: PrettyGrid<MethodLocations> = buildPrettyGrid{
            onResolved {
                headedRow(header)
                buildRow {
                    add(methodName)
                }
            }
            useTemplate(StackFrameMeta.linkTemplate, MethodLocations::frames)
        }
    }
}