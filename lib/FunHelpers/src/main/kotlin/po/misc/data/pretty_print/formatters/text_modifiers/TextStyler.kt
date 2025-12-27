package po.misc.data.pretty_print.formatters.text_modifiers

import po.misc.data.pretty_print.cells.PrettyCellBase
import po.misc.data.pretty_print.parts.options.CellOptions
import po.misc.data.pretty_print.parts.options.Style
import po.misc.data.styles.BGColour
import po.misc.data.styles.TextStyler

class CellStyler(
    val cell: PrettyCellBase
): TextModifier{

    override val dynamic: Boolean = false
    override val formatter: Formatter = Formatter.TextStyler

    val options: CellOptions = cell.cellOptions

    override fun modify(text: String): String {
        val useStyle = options.style.textStyle
        val useColour = options.style.colour
        val useBackgroundColour = options.style.backgroundColour
        return style(text, useStyle, useColour, useBackgroundColour?: BGColour.Default)
    }

    fun modify(text: String, styleOptions: Style): String {
        val useStyle = options.style.textStyle
        val useColour = styleOptions.colour
        val useBackgroundColour = styleOptions.backgroundColour
        return style(text,  useStyle, useColour, useBackgroundColour?: BGColour.Default)
    }

}