package po.test.misc.data.pretty_print.rows

import po.misc.data.output.output
import po.misc.data.pretty_print.PrettyGrid
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.parts.common.Grid
import po.misc.data.pretty_print.parts.common.Row
import po.misc.data.pretty_print.parts.grid.RenderPlan
import po.misc.data.pretty_print.parts.options.Align
import po.misc.data.pretty_print.parts.options.BorderPresets
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.parts.options.RowOptions
import po.misc.data.pretty_print.parts.options.ViewPortSize
import po.misc.data.pretty_print.parts.rows.Layout
import po.misc.data.pretty_print.parts.template.RowDelegate
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf
import po.test.misc.data.pretty_print.setup.PrettyTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestRowDecoration: PrettyTest<TestRowDecoration>(true){
    private class SubClass(val name: String = "SubClass", val value: Int = 300)

    override val receiverType: TypeToken<TestRowDecoration> = tokenOf()


    private val stretchedOption = RowOptions{
        layout = Layout.Stretch
        borders(BorderPresets.Box)
    }
    private val compactOption = RowOptions{
        layout = Layout.Compact
        borders(BorderPresets.Box)
    }


    private val shortText = "Text 1"
    private val shortText2 = "Text 2"
    private val bitLongerText = "Text 2 bit longer"
    private val subClass = SubClass()

    @Test
    fun `Compact rows take least possible space within upper container`() {
        val rowOption = RowOptions(Orientation.Horizontal)
        rowOption.borders(BorderPresets.Box)
        val grid = buildGrid {
            options.viewport = ViewPortSize.Console80
            useID(Grid.Grid1)
            buildRow(Row.Row1){
                applyOptions(rowOption)
                add(TestRowDecoration::shortText)
                add(TestRowDecoration::bitLongerText)
            }
            buildRow(TestRowDecoration::subClass, Row.Row2) {
                applyOptions(rowOption)
                add(SubClass::name)
                add(SubClass::value)
            }
        }
        val render = grid.render(this)
        val lines = render.lines()
        render.output(enableOutput)
        val firstLine = lines.first()
        val lastLine = lines.last()
        assertEquals(6, lines.size)
        assertTrue { firstLine.length < ViewPortSize.Console80.size && firstLine.length > lastLine.length }
    }

    @Test
    fun `Stretched rows take all possible space within upper container`() {
        val delegate = RowDelegate<TestRowDecoration>()
        val grid = buildGrid(Grid.Grid1) {
            options.viewport = ViewPortSize.Console80
            options.layout = Layout.Stretch
            buildRow(Row.Row1){
                applyOptions(compactOption)
                add(TestRowDecoration::shortText)
                add(TestRowDecoration::shortText2)
                acceptDelegate(delegate)
            }
        }
        assertEquals(Layout.Stretch, grid.keyParameters.layout)
        assertEquals(Layout.Compact, delegate.keyParameters.layout)
        val render = grid.render(this)
        val lines = render.lines()
        render.output(enableOutput)
    }
}