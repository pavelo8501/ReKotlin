package po.test.misc.data.pretty_print.rows

import po.misc.data.output.output
import po.misc.data.pretty_print.PrettyValueRow
import po.misc.data.pretty_print.Templated
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.parts.options.RowOptions
import po.misc.data.pretty_print.parts.options.ViewPortSize
import po.misc.data.pretty_print.parts.rows.RowLayout
import po.misc.data.styles.Colour
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestValueRowRenderParams : Templated<TestValueRowRenderParams> {

    override val type: TypeToken<TestValueRowRenderParams> = tokenOf()

    private val text1 = "Text 1"
    private val text2 = "Text 2 bit longer"
    private val text3 = "Text 3 longer than text 2"

    private val keyedCell = ::text3.toCell()
    private val enableOutput = true

    private val self =  this


    @Test
    fun `Row indention for RowLayout Centered`() {
        val bottomBorderChar = '-'
        val options = RowOptions(Orientation.Horizontal, RowLayout.Centered, ViewPortSize.Console120)
        options.borders(borderColour = Colour.Green,  bottomBorder = bottomBorderChar)
        val staticCell1 = text1.toCell()
        val staticCell2 = text2.toCell()
        val row = PrettyValueRow(TestValueRowRenderParams::self, options, listOf(staticCell1, staticCell2, keyedCell))
        val render = row.renderFromSource(this)
        render.output(enableOutput)
        val lines = render.lines()
        assertEquals(2, lines.size)
        assertTrue { lines[0].contains(text1) && lines[0].contains(text2) }
        assertTrue { lines[1].contains(bottomBorderChar) }
    }
}