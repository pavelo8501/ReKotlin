package po.test.misc.data.pretty_print.rows

import po.misc.data.output.output
import po.misc.data.pretty_print.parts.common.Row
import po.misc.data.pretty_print.parts.options.BorderPresets
import po.misc.data.pretty_print.parts.options.Options
import po.misc.data.pretty_print.parts.options.RowOptions
import po.misc.data.pretty_print.parts.rows.Layout
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf
import po.test.misc.data.pretty_print.setup.PrettyTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RowDecorationTest: PrettyTest<RowDecorationTest>(){

    private class SubClass(val name: String = "SubClass", val value: Int = 300)

    override val receiverType: TypeToken<RowDecorationTest> = tokenOf()

    private val stretchedOption = RowOptions{
        layout = Layout.Stretch
        borders(BorderPresets.Box)
    }
    private val compactOption = RowOptions{
        layout = Layout.Compact
        borders(BorderPresets.Box)
    }
    private val cellOption = Options{
        borders(BorderPresets.HalfBox)
    }

    private val shortText = "Text 1"
    private val shortText2 = "Text 2"
    private val bitLongerText = "Text 2 bit longer"
    private val subClass = SubClass()
    private val self = this

    @Test
    fun `Compact row`(){
        val sizeSeparatorsSize = 2
        val whiteSpaceSize = 1
        val expectedSize = shortText.length + shortText2.length + sizeSeparatorsSize + whiteSpaceSize
        val row = buildRow(Row.Row1){
            applyOptions(compactOption)
            add(shortText)
            add(shortText2)
        }
        val renderCanvas = with(row){
            renderInScope(self)
        }
        renderCanvas.output(testVerbosity)
        val lines = renderCanvas.plain.lines()
        assertEquals(3, lines.size)
        assertEquals(expectedSize, lines[0].length)
        assertEquals(expectedSize, lines[1].length)
        assertEquals(expectedSize, lines[2].length)
    }
    @Test
    fun `Compact row witch cell decoration`(){
        val cell1 = shortText.toCell(cellOption)
        val cell2 = shortText2.toCell(cellOption)
        val row = buildRow(Row.Row1){
            applyOptions(compactOption)
            add(cell1)
            add(cell2)
        }
        val renderCanvas = with(row){
            renderInScope(self)
        }
        renderCanvas.output(testVerbosity)
        val lines = renderCanvas.plain.lines()
    }
}