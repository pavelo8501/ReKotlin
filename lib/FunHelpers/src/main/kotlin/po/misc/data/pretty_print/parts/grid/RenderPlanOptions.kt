package po.misc.data.pretty_print.parts.grid

import po.misc.data.PrettyPrint
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.PrettyValueGrid
import po.misc.data.strings.appendLine
import po.misc.data.styles.SpecialChars

data class RenderPlanSnapshot (
    val renderPlan: RenderPlan<*, *>
): PrettyPrint{

    val displayName: String = renderPlan.displayName

    private val rowSize = renderPlan[PrettyRow::class].size

    val rowsCount: String get() = "${PrettyRow.prettyName}: $rowSize"
    val valueGridsSize :Int = renderPlan[PrettyValueGrid::class].size
    val valueGridsCount: String get() = "${PrettyValueGrid.prettyName}: $valueGridsSize"
    val allRenderables: String get() {
        return  renderPlan.getNodesOrdered().joinToString(SpecialChars.NEW_LINE) {
            it.toString()
        }
    }

    override val formattedString: String get(){
        return buildString {
            appendLine(displayName)
            appendLine(rowsCount)
            appendLine(valueGridsCount)
            appendLine(allRenderables)
        }

    }
    override fun toString(): String = buildString {
        appendLine(displayName)
        appendLine("Rows: $rowSize")
        appendLine("ValueGrids: $valueGridsSize")
        appendLine(allRenderables)
    }
}
