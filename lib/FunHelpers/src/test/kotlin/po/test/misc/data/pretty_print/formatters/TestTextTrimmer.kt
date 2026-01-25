package po.test.misc.data.pretty_print.formatters

import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.parts.options.RowOptions
import po.misc.data.styles.TextStyler
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf
import po.test.misc.data.pretty_print.setup.PrettyTest



class TestTextTrimmer: PrettyTest<TestTextTrimmer>(true), TextStyler  {

    override val receiverType: TypeToken<TestTextTrimmer> = tokenOf()

    private val rowOptions= RowOptions( Orientation.Horizontal)
    private val text: String = "Longer text should be trimmed to initial width of the cell"


//    @Test
//    fun `Text trimmed`(){
//        val trimmer  = TextTrimmer("...")
//        val text = "Five5"
//        val expectedResult = "F..."
//        val result = trimmer.modify(text, parameters)
//        assertEquals(expectedResult, result)
//    }
//
//    @Test
//    fun `Text trimmed for formatted pair preserving ansi string`(){
//        val trimmer  = TextTrimmer("...")
//        val text = "Five5"
//        val formattedText = "Five5".colorize(Colour.GreenBright)
//        val expectedResult = "F..."
//        val expectedFormattedResult = "F...".colorize(Colour.GreenBright)
//        val pair = FormattedText(text, formattedText)
//        trimmer.modify(pair, parameters)
//        pair.output(enableOutput)
//        assertEquals(expectedResult, pair.plain)
//        assertEquals(expectedFormattedResult, pair.formatted)
//    }
//
//    @Test
//    fun `Text trimmed for text preserving ansi string`(){
//        val trimmer  = TextTrimmer("...")
//        val formattedText = "Five5".colorize(Colour.GreenBright)
//        val expectedFormattedResult = "F...".colorize(Colour.GreenBright)
//        val result =  trimmer.modify(formattedText, parameters)
//        result.output(enableOutput)
//        assertEquals(expectedFormattedResult, result)
//    }

//    @Test
//    fun `Text trim for RenderRecord`(){
//        val options = Options()
//        val trimmer  = TextTrimmer(options.trimSubstitution)
//        val node = BoundRenderNode(::text.toCell(), 0, 1, rowOptions)
//        val totalSizeAfterTrim = 11
//        node.render(this)
//        val renderRecord = assertNotNull(node.renderRecord)
//        node.reRenderWidth = totalSizeAfterTrim
//        trimmer.modify(renderRecord, node)
//        renderRecord.output(enableOutput)
//
//        assertEquals(totalSizeAfterTrim,  renderRecord.toString().lengthNoAnsi)
//        assertTrue {
//            renderRecord.formattedKey.contains(options.keyStyle.colour)
//            renderRecord.formattedKey.contains(::text.displayName)
//        }
//        assertEquals("Lon...", renderRecord.formattedValue)
//    }


}