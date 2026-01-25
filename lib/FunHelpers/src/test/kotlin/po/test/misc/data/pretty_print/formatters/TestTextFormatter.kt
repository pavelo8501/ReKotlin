package po.test.misc.data.pretty_print.formatters

import po.misc.data.pretty_print.formatters.TextFormatter
import po.misc.data.pretty_print.formatters.text_modifiers.CellStyler
import po.misc.data.pretty_print.formatters.text_modifiers.TextTrimmer
import po.misc.data.pretty_print.parts.options.Options
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.parts.options.RowOptions
import po.misc.data.styles.TextStyler

class TestTextFormatter : TextStyler {

    private val text = "Some text"
    private val longerText = "Longer text should be trimmed to initial width of the cell"
    private val enableOutput:Boolean = true
    private val options = Options()

    private val cellStyler = CellStyler()
    private val textTrimmer  = TextTrimmer("...")
    private val textFormatter = TextFormatter(cellStyler, textTrimmer)
    private val rowOptions = RowOptions(Orientation.Horizontal)

//    @Test
//    fun `Cell styling + trim`(){
//        val styledOptions = Options()
//        styledOptions.style(TextStyle.Regular, Colour.Blue)
//        val formattedText = FormattedText(longerText)
//        val cellParameters = CellParameters(index = 0, totalCells =  0, options, rowOptions)
//        cellParameters.width = 9
//        textFormatter.format(formattedText, styledOptions)
//        formattedText.output(enableOutput)
//        assertTrue { formattedText.formatted.contains(longerText) && formattedText.formatted.contains(Colour.Blue) }
//
//        val projectedResult = "Longer..."
//        textFormatter.format(formattedText, cellParameters)
//        formattedText.output(enableOutput)
//        formattedText.plain.output(enableOutput)
//        assertTrue{ formattedText.formatted.contains(projectedResult) && formattedText.formatted.contains(Colour.Blue)  }
//        assertTrue{ formattedText.plain.contains(projectedResult) && !formattedText.plain.isStyled  }
//        assertEquals(formattedText.plain, formattedText.formatted.stripAnsi())
//    }
//
//    @Test
//    fun `Formatter respects Key parameters values`(){
//        val cellParameters = CellParameters(index = 0, totalCells =  0, options, rowOptions)
//        cellParameters.width = 10
//        val trimmer  = TextTrimmer("...")
//        val formatter = TextFormatter(trimmer)
//
//        var cell = StaticCell(text, options)
//        var formatted = cell.resolve()
//        formatter.format(formatted, cellParameters)
//        formatted.output(enableOutput)
//        assertEquals(text, formatted.plain)
//        cell = StaticCell(longerText, options)
//        formatted = cell.resolve()
//        formatter.format(formatted, cellParameters)
//        formatted.output(enableOutput)
//        assertEquals(cellParameters.width, formatted.plain.length)
//    }

}