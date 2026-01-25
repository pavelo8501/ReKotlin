package po.test.misc.data.pretty_print.formatters

import po.misc.data.pretty_print.formatters.text_modifiers.CellStyler
import po.misc.data.pretty_print.parts.options.Options
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.parts.options.RowOptions
import po.misc.data.strings.contains
import po.misc.data.styles.Colour
import po.misc.data.styles.TextStyle
import po.misc.data.styles.TextStyler
import po.misc.data.text_span.FormattedText
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.text.contains

class TestCellStyler: TextStyler {

    private val longerText = "Longer text should be trimmed to initial width of the cell"
    private val styler = CellStyler()
    private val rowOptions = RowOptions(Orientation.Horizontal)

    @Test
    fun `Cell styling value`() {

        val styledOptions = Options()
        styledOptions.style(TextStyle.Regular, Colour.Blue)
        val formattedText = FormattedText(longerText)
        styler.modify(formattedText, styledOptions)
        assertTrue { formattedText.styled.contains(longerText) && formattedText.styled.contains(Colour.Blue) }
        assertTrue { formattedText.plain.contains(longerText) &&  !formattedText.plain.isStyled }
    }


//    @Test
//    fun `Cell styling key + value`(){
//
//        val styledOptions = Options()
//        styledOptions.keyStyle(TextStyle.Regular, Colour.Green)
//        styledOptions.style(TextStyle.Regular, Colour.Blue)
//
//        val plainKeyText = "Key1"
//
//
//        val cellParameters = CellParameters(index = 0, totalCells =  0, styledOptions, rowOptions)
//        cellParameters.width = 10
//
//        val formatted = FormattedText()
//        val keyText = PrettyCellBase.KeyValueTags.Key.createFormatted(plainKeyText)
//        val valueText = PrettyCellBase.KeyValueTags.Value.createFormatted(longerText)
//
//        formatted.addAll(keyText, valueText)
//        styler.modify(formatted, styledOptions)
//
//        formatted.joinNamedEntries(separator = styledOptions.keySeparator.toString(), PrettyCellBase.KeyValueTags.Key, PrettyCellBase.KeyValueTags.Value)
//        val split = formatted.formatted.split(styledOptions.keySeparator.toString())
//        assertEquals(2, split.size)
//        assertTrue { split[0].contains(plainKeyText) &&  split[0].contains(Colour.Green) }
//        assertTrue { split[1].contains(longerText) &&  split[1].contains(Colour.Blue) }
//
//    }


}