package po.test.misc.data.pretty_print.rows

import po.misc.data.output.output
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.cells.PrettyCell
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.parts.options.RowOptions
import po.misc.data.pretty_print.parts.rows.Layout
import po.test.misc.data.pretty_print.setup.PrettyTestBase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestRowInitialization : PrettyTestBase(){

    private val someText1 = "Text 1"
    private val someText2 = "Text 2"
    private val someText3 = "Text 3"

    @Test
    fun `Row precomputes cells with parameters`() {
        val cell = someText1.toCell()
        val cell2 = someText2.toCell()
        val cell3 = someText3.toCell()
        val options = RowOptions(Orientation.Horizontal, Layout.Stretch)
        val row = PrettyRow(options, cell, cell2, cell3)
        row.planner.nodes.output()
        val render = row.render()
        render.output(verbosity)
        val lines = render.lines()
        assertEquals(3, row.planner.nodes.size)
        assertEquals(1, lines.size)
        assertTrue { lines[0].length >= (options.viewport?.size?:0) - 2 }
    }

    @Test
    fun `Row rendering with precomputed static and pretty cells`() {
        val prettyString = "Pretty cell text"
        val cell = someText1.toCell()
        val pretty = PrettyCell()
        val options = RowOptions(Orientation.Horizontal, Layout.Stretch)
        val row = PrettyRow(options, cell, pretty)
        row.planner.nodes.output(verbosity)
        assertEquals(2, row.planner.nodes.size)
        val render = row.renderAny(prettyString)
        render.output(verbosity)
    }
}