package po.misc.data.pretty_print.formatters.text_modifiers

import po.misc.data.pretty_print.cells.PrettyCellBase
import po.misc.data.pretty_print.parts.CellOptions
import po.misc.data.pretty_print.parts.Style
import po.misc.data.styles.TextStyler

class CellStyler(
    val cell: PrettyCellBase
): TextModifier{

    override val formatter: Formatter = Formatter.TextStyler

    val options: CellOptions = cell.cellOptions

    override fun modify(text: String): String {
        val useStyle = options.style.textStyle
        val useColour = options.style.colour
        val useBackgroundColour = options.style.backgroundColour
        return TextStyler.style(text, applyColourIfExists = false, useStyle, useColour, useBackgroundColour)
    }

    fun modify(text: String, styleOptions: Style): String {
        val useStyle = options.style.textStyle
        val useColour = styleOptions.colour
        val useBackgroundColour = styleOptions.backgroundColour
        return TextStyler.style(text, applyColourIfExists = false, useStyle, useColour, useBackgroundColour)
    }

}