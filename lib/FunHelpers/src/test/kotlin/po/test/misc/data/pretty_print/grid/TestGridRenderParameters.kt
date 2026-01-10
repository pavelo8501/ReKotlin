package po.test.misc.data.pretty_print.grid

import po.misc.data.output.output
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.parts.options.RowOptions
import po.misc.data.pretty_print.parts.options.ViewPortSize
import po.misc.data.pretty_print.parts.rows.RowLayout
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf
import po.test.misc.data.pretty_print.setup.PrettyTest
import kotlin.test.Test
import kotlin.test.assertEquals


class TestGridRenderParameters : PrettyTest<TestGridRenderParameters>(enableOutput = true) {

    override val type: TypeToken<TestGridRenderParameters> = tokenOf()

    private val text1 = "Text 1"
    private val text2 = "Text 2 bit longer"
    private val text3 = "Text 3 longer than text 2"
    private val text4 = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt"

    private val keyedCell = ::text3.toCell()

    private val self =  this

    private val static1 = text1.toCell()
    private val static2 = text2.toCell()
    private val static3 = text3.toCell()
    private val static4 = text4.toCell()

    @Test
    fun `Grid impose width constraints on the row render`() {
        val gridOpts = RowOptions(Orientation.Horizontal, RowLayout.Compact, ViewPortSize.Console80)
        val rowOpts = RowOptions(Orientation.Horizontal, RowLayout.Compact, ViewPortSize.Console220)
        val row = PrettyRow<TestGridRenderParameters>(static1, static2, static3, static4)
        row.applyOptions(rowOpts)
        val grid = buildGrid { applyOptions(gridOpts); addRow(row) }

        assertEquals(grid.parameters.declaredWidth, row.planner.parameters.declaredWidth)
        val render = grid.render(this)
        row.planner.output(enableOutput)
        render.length.output()
        render.output(enableOutput)
    }

//    @Test
//    fun `Rows accept grid options if own were created by default`() {
//        val options = RowOptions(Orientation.Horizontal, RowLayout.Compact)
//        val staticRow = PrettyRow<TestGridRenderParams>(staticCell1, staticCell2)
//        val keyedRow = PrettyRow<TestGridRenderParams>(staticCell1, keyedCell)
//        val grid = PrettyGrid<TestGridRenderParams>(opts = options)
//        grid.addRows(listOf(staticRow, keyedRow))
//        assertEquals(options, staticRow.options)
//        assertEquals(options, keyedRow.options)
//    }
//
//    @Test
//    fun `Rows use their own options if explicitly provided`() {
//        val options = RowOptions(Orientation.Horizontal, RowLayout.Compact)
//        val rowOption = RowOptions(Orientation.Horizontal, RowLayout.Centered)
//        val staticRow = PrettyRow<TestGridRenderParams>(staticCell1, staticCell2)
//        staticRow.applyOptions(rowOption)
//        val keyedRow = PrettyRow<TestGridRenderParams>(staticCell1, keyedCell)
//        val grid = PrettyGrid<TestGridRenderParams>(opts = options)
//        grid.addRows(listOf(staticRow, keyedRow))
//        assertFalse { keyedRow.areOptionsExplicit  }
//        assertNotEquals(options, staticRow.options)
//        assertEquals(rowOption, staticRow.options)
//        assertEquals(options, keyedRow.options)
//        val renderTimeOptions = RowOptions(Orientation.Vertical, RowLayout.Compact, ViewPortSize.Console80)
//        grid.render(this, renderTimeOptions)
//        assertNotEquals(renderTimeOptions, staticRow.currentRenderOpt)
//        assertEquals(renderTimeOptions, keyedRow.currentRenderOpt)
//    }
//
//    @Test
//    fun `Rows obey size constraints imposed by upper container`(){
//
//        val rowOption = RowOptions(Orientation.Horizontal, RowLayout.Stretch, ViewPortSize.Console220)
//        val row = PrettyRow<TestGridRenderParams>(staticCell1, staticCell2)
//        row.applyOptions(rowOption)
//        assertEquals(RowLayout.Stretch, row.planner.parameters.layout)
//        val gridOption = RowOptions(Orientation.Horizontal, RowLayout.Compact, ViewPortSize.Console80)
//        val grid = PrettyGrid<TestGridRenderParams>(opts = gridOption)
//        grid.addRow(row)
//        assertEquals(rowOption, row.options)
//        assertEquals(ViewPortSize.Console80.value, row.planner.parameters.maxWidth)
//        val render = grid.render(this)
//        render.output(enableOutput)
//        val split = render.split('|')
//        assertEquals(2, split.size)
//        assertEquals(ViewPortSize.Console80.value / 2, split[0].length)
//    }


}