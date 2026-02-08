package po.test.misc.data.pretty_print.parts.decorator

import po.misc.data.output.output
import po.misc.data.pretty_print.PrettyValueGrid
import po.misc.data.pretty_print.PrettyValueRow
import po.misc.data.pretty_print.parts.common.Grid
import po.misc.data.pretty_print.parts.common.RenderData
import po.misc.data.pretty_print.parts.common.Row
import po.misc.data.pretty_print.parts.options.BorderPresets
import po.misc.data.pretty_print.parts.options.GridID
import po.misc.data.pretty_print.parts.options.ViewPortSize
import po.misc.data.pretty_print.parts.rows.Layout
import po.misc.data.pretty_print.parts.template.TemplateDelegate
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf
import po.test.misc.data.pretty_print.setup.PrettyTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestTemplateDecoration: PrettyTest<TestTemplateDecoration>() {

    private class SubClass(val name: String = "SubClass")

    override val receiverType: TypeToken<TestTemplateDecoration> = tokenOf()
 
    private val subClass = SubClass()
    private val shortText = "Text 1"
    private val bitLongerText = "Text 2 bit longer"
    private val longerText = "Text 3 longer than text 2"

    private val shortTextSize = shortText.length
    private val bitLongerTextSize = bitLongerText.length
    private val longerTextSize = longerText.length

    @Test
    fun `Stacked rows are separated by default`() {
        val grid = buildGrid {
            buildRow { add(TestTemplateDecoration::shortText) }
            buildRow { add(TestTemplateDecoration::bitLongerText) }
        }
        val render = grid.render(this)
        val lines = render.lines()
        assertEquals(2, lines.size)
    }

    @Test
    fun `Stacked grids are separated by default`() {
        val grid = buildGrid {
            buildRow(Row.Row1) { add(TestTemplateDecoration::shortText) }
            buildGrid(TestTemplateDecoration::subClass) {
                useID(Grid.Grid2)
                buildRow(Row.Row2) { add(SubClass::name) }
            }
        }
        assertEquals(Row.Row1, grid.rows[0].templateID, "Row1 id not assigned")
        assertNotNull(grid.renderPlan[PrettyValueGrid].firstOrNull()) { valueGrid ->
            assertEquals(Grid.Grid2, valueGrid.templateID)
            assertEquals(Row.Row2, valueGrid.rows[0].templateID, "Row2 id not assigned")
        }
        val render = grid.render(this)
        assertEquals(2, render.lines().size)
    }

    @Test
    fun `Lower container respects max size of upper`() {
        val gridDelegate = TemplateDelegate<SubClass>()
        val grid = buildGrid {
            useID(Grid.Grid1)
            options.viewport = ViewPortSize.Console40

            buildRow(Row.Row1) { add(TestTemplateDecoration::shortText) }

            buildGrid(TestTemplateDecoration::subClass){
                acceptDelegate(gridDelegate)
                options.viewport = ViewPortSize.Console80
                options.layout = Layout.Stretch
                options.borders(BorderPresets.Box)
                useID(Grid.Grid2)
                buildRow(Row.Row2) { add(SubClass::name) }
            }
        }

        assertEquals(ViewPortSize.Console40.size, grid.renderPlan.maxWidth, "Max wide Grid1 not updated")
        assertEquals(ViewPortSize.Console80.size, gridDelegate.keyParameters.maxWidth, "Max wide Grid2 not updated")
       // assertTrue("Borders not applied to Grid2"){ gridDelegate.decorator.enabled }
        val render = grid.render(this)
        val lines =  render.lines()
        assertEquals(4, lines.size)
        assertEquals(ViewPortSize.Console40.size -1, lines[0].length)
    }

    @Test
    fun `Lower container respects max size of upper with render data `() {
        val gridDelegate = TemplateDelegate<SubClass>()
        val grid = buildGrid {
            useID(Grid.Grid1)
            options.viewport = ViewPortSize.Console40
            buildRow(Row.Row1) { add(TestTemplateDecoration::shortText) }
            buildGrid(TestTemplateDecoration::subClass){
                acceptDelegate(gridDelegate)
                options.viewport = ViewPortSize.Console80
                options.layout = Layout.Stretch
                options.borders(BorderPresets.Box)
                useID(Grid.Grid2)
                buildRow(Row.Row2) { add(SubClass::name) }
            }
        }
        assertEquals(ViewPortSize.Console40.size, grid.renderPlan.maxWidth, "Max wide Grid1 not updated")
        assertEquals(ViewPortSize.Console80.size, gridDelegate.keyParameters.maxWidth, "Max wide Grid2 not updated")
       // assertTrue("Borders not applied to Grid2"){ gridDelegate.decorator.enabled }

        val render = grid.render(this)
        render.output(testVerbosity)
        val lines =  render.lines()
        assertEquals(4, lines.size)
        assertEquals(ViewPortSize.Console40.size, lines[3].length)
    }

    @Test
    fun `Containers with left margin properly distributed`() {
        val gridDelegate = TemplateDelegate<SubClass>()
        val grid = buildGrid(Grid.Grid1) {
            options.borders(BorderPresets.Box)
            buildRow(Row.Row1) { add(TestTemplateDecoration::shortText) }
            buildRow(TestTemplateDecoration::subClass){
                acceptDelegate(gridDelegate)
                add(SubClass::name)
            }
            buildRow(Row.Row2) { add(TestTemplateDecoration::bitLongerText) }
        }
        val row = assertIs< PrettyValueRow<TestTemplateDecoration, SubClass>>(gridDelegate.host)
        assertTrue { row.dataLoader.canResolve }
        val render = grid.render(this)
        render.output()
    }

    @Test
    fun `Containers new`() {

        val grid = buildGrid(Grid.Grid1) {
            options.borders(BorderPresets.Box, Colour.MagentaBright)
            buildRow{
                add(TestTemplateDecoration::shortText)
                add(TestTemplateDecoration::bitLongerText)
                add(TestTemplateDecoration::shortTextSize)
                add("Kakojto statichnij")

                add(TestTemplateDecoration::shortTextSize){
                    "$this A stal takoj".colorize(Colour.Green)
                }
            }
        }
        val render =  grid.render(this)
        render.output()
    }
}