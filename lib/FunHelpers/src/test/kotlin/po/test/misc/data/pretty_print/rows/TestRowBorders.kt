package po.test.misc.data.pretty_print.rows

import po.misc.collections.third
import po.misc.data.output.output
import po.misc.data.output.outputCompare
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.cells.KeyedCell
import po.misc.data.pretty_print.parts.decorator.BorderPosition
import po.misc.data.pretty_print.parts.common.Row
import po.misc.data.pretty_print.parts.options.BorderPresets
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.parts.options.RowOptions
import po.misc.data.pretty_print.toCell
import po.misc.data.strings.contains
import po.misc.data.styles.Colour
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf
import po.test.misc.data.pretty_print.setup.PrettyTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


class TestRowBorders : PrettyTest<TestRowBorders>() {

    private class SubClass(val name:String = "SubClass")
    
    override val receiverType: TypeToken<TestRowBorders> = tokenOf()

    private val subClass = SubClass()
    private val shortText = "Text 1"
    private val bitLongerText = "Text 2 bit longer"
    private val text3 = "Text 3 longer than text 2"

    private val vertical get() =  BorderPresets.VerticalBorders("*", Colour.Blue)
    private val horizontal get() =  BorderPresets.HorizontalBorders("|", Colour.MagentaBright)

    @Test
    fun `Row positioning inside top container`() {

        val gridOptions = RowOptions(Orientation.Horizontal)
        gridOptions.borders(vertical)
        gridOptions.borders(horizontal)
        val rowOptions = RowOptions(Orientation.Horizontal)
        rowOptions.borders(BorderPresets.HalfBox, Colour.Green)
        val cell1 = ::shortText.toCell()
        val cell2 = ::bitLongerText.toCell()
        val row = PrettyRow(rowOptions, cell1, cell2)
        val grid = buildGrid {
            applyOptions(gridOptions)
            addRow(row)
        }
        val render = grid.render(this)
        render.output()
        val lines = render.lines()
        assertTrue("Grid's separators disabled") { gridOptions.hasEnabledSeparators }
        assertTrue("Row's separators disabled") { rowOptions.hasEnabledSeparators }
        assertEquals(5, lines.size)
        assertTrue {
            with(lines.third()){
                contains(shortText) &&
                contains(bitLongerText) &&
                contains(Colour.MagentaBright) &&
                contains(KeyedCell.keyedOption.style.colour)
            }
        }
    }

    @Test
    fun `Two rows inside grid both bordered`() {
        val cell1 = text3.toCell()
        val row1 = PrettyRow<TestRowBorders>(cell1)
        row1.options.borders(BorderPresets.Box, Colour.Blue)
        val keyCell1 = ::shortText.toCell()
        val keyCell2 = ::bitLongerText.toCell()
        val row2 = PrettyRow(keyCell1, keyCell2)
        row2.options.borders(BorderPresets.Box, Colour.Green)
        val grid = buildGrid {
            addRow(row1)
            addRow(row2)
            options.orientation = Orientation.Vertical
            options.borders(BorderPresets.Box, Colour.YellowBright)
        }
        val renderPlan = grid.renderPlan
        assertTrue { renderPlan.decorator.enabled }
        val render = grid.render(this)
        render.output()
    }

    @Test
    fun `Two grids containing 1 row each`() {
        val keyCell1 = toCell(SubClass::name)
        val row1 = PrettyRow<SubClass>(keyCell1)
        val keyCell2 = ::shortText.toCell()
        val row2 = PrettyRow<TestRowBorders>(keyCell2)
        val grid = buildGrid {
            buildGrid(TestRowBorders::subClass){
                addRow(row1)
            }
            addRow(row2)
        }
        val render = grid.render(this)
        render.output()
    }

}