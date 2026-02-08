package po.test.misc.data.pretty_print.grid

import po.misc.data.output.output
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.parts.options.RowOptions
import po.misc.data.pretty_print.parts.options.ViewPortSize
import po.misc.data.pretty_print.parts.rows.Layout
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf
import po.test.misc.data.pretty_print.setup.PrettyTest
import kotlin.test.Test
import kotlin.test.assertEquals


class TestGridRenderParameters : PrettyTest<TestGridRenderParameters>() {

    override val receiverType: TypeToken<TestGridRenderParameters> = tokenOf()

    private val text1 = "Text 1"
    private val text2 = "Text 2 bit longer"
    private val text3 = "Text 3 longer than text 2"
    private val text4 = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt"

    private val static1 = text1.toCell()
    private val static2 = text2.toCell()
    private val static3 = text3.toCell()
    private val static4 = text4.toCell()

    @Test
    fun `Grid impose width constraints on the row render`() {
        val gridOpts = RowOptions(Orientation.Horizontal, Layout.Compact, ViewPortSize.Console80)
        val rowOpts = RowOptions(Orientation.Horizontal, Layout.Compact, ViewPortSize.Console180)
        val row = PrettyRow<TestGridRenderParameters>(static1, static2, static3, static4)
        row.applyOptions(rowOpts)
        val grid = buildGrid { applyOptions(gridOpts); addRow(row) }

        assertEquals(grid.renderPlan.maxWidth, row.planner.maxWidth)
        val render = grid.render(this)
    }
}