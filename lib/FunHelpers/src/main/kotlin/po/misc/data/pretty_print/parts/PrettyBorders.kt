package po.misc.data.pretty_print.parts

import po.misc.data.helpers.orDefault
import po.misc.data.pretty_print.cells.PrettyCellBase

class PrettyBorders(
    var leftBorder: Char? = null,
    var rightBorder: Char? = null,
){

    private val left get() =  leftBorder.orDefault{ "$it " }
    private val right get() =  rightBorder.orDefault{ " $it" }

    fun renderLeft(content: String,  postfixRight: String = ""): String{
        return "$left$content$postfixRight"
    }
    fun renderRight(content: String, postfixRight: String = " "): String{
        return "$content$right$postfixRight"
    }

    fun render(content: String): String{
        return "$left$content$right"
    }
    fun render(content: String, cell: PrettyCellBase, cellsCount: Int): String{
        val options = cell.cellOptions
        return when {
            cell.orientation == Orientation.Vertical -> content
            cellsCount == 1 -> content
            options.renderLeftBorder && options.renderRightBorder -> render(content)
            options.renderLeftBorder -> renderLeft(content)
            options.renderRightBorder -> renderRight(content)
            else -> content
        }
    }
}