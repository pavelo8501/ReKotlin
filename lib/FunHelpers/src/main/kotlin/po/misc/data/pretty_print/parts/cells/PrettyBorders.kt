package po.misc.data.pretty_print.parts.cells

import po.misc.data.pretty_print.cells.PrettyCellBase
import po.misc.data.pretty_print.parts.options.Orientation

class PrettyBorders(
    var leftBorder: String= "|",
    var rightBorder: String= "|",
){
    fun renderLeft(content: String): String{
        return " $leftBorder $content"
    }
    fun renderRight(content: String): String{
        return "$content $rightBorder"
    }
    fun render(content: String): String{
        return "$leftBorder$content$rightBorder"
    }
    fun render(content: String, cell: PrettyCellBase): String{
        val row = cell.row ?: return content
        if(cell.orientation  == Orientation.Vertical  || ! cell.renderBorders){
            return content
        }
        leftBorder = row.currentRenderOpt.borderSeparator
        rightBorder = row.currentRenderOpt.borderSeparator
        val lastCell = cell.index >= cell.cellsCount -1
        return when {
            cell.cellsCount == 1 -> content
            cell.index == 0 -> renderRight(content)
            cell.cellsCount > 2 && lastCell -> renderLeft(content)
            lastCell ->  content
            else -> content
        }
    }
}