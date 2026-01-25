package po.test.misc.data.pretty_print.grid

import po.misc.data.output.output
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.PrettyValueGrid
import po.misc.data.pretty_print.buildGrid
import po.misc.data.pretty_print.cells.KeyedCell
import po.misc.data.pretty_print.dsl.InTestDSL
import po.misc.data.pretty_print.buildPrettyGrid
import po.misc.data.pretty_print.parts.common.Grid
import po.test.misc.data.pretty_print.setup.PrettyTestBase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestValueGridRendering :PrettyTestBase(){

    class SomeClass(val name: String = "SomeClass")
    private  val inTestEngine = InTestDSL()
    private val some = SomeClass()
    private val someClassGrid = buildPrettyGrid<SomeClass> {
        buildRow {
            add(SomeClass::name)
            add("Static")
        }
    }

    @Test
    fun `PrettyValueGrid grid creation`(){
        val grid = buildGrid<ReportClass>{
            buildListGrid(ReportClass::entries){
                addRow(PrettyRow(KeyedCell(ReportEntry::name)))
            }
        }
       assertNotNull(grid.renderPlan[PrettyValueGrid].firstOrNull()){prettyGrid->
           assertEquals(1, prettyGrid.rows.size)
           val row = assertIs<PrettyRow<ReportEntry>>(prettyGrid.rows[0])
           assertEquals(1, row.cells.size)
       }
        val report = ReportClass(entryCount =  10)
        val render =  grid.render(report)
        render.output(enableOutput)
    }

    @Test
    fun `PrettyValueGrid grid rendering`(){

        val grid = inTestEngine.buildGrid<TestValueGridRendering> {
            useGrid(someClassGrid, TestValueGridRendering::some)
        }
        assertEquals(1, grid.renderPlan.renderNodes.size, "Render nodes count different")
        assertNotNull(grid.renderPlan.renderNodes.firstOrNull()){
            assertIs<PrettyValueGrid<TestValueGridRendering, SomeClass>>(it.element)
        }

        assertNotNull(grid.renderPlan[PrettyValueGrid].firstOrNull()){prettyGrid->
            assertEquals(1, prettyGrid.rows.size, "Rows not copied")
            val row = assertIs<PrettyRow<ReportEntry>>(prettyGrid.rows[0])
            assertEquals(2, row.cells.size, "$row cells count different")
        }
        val render =  grid.render(this)
        render.output(enableOutput)
        val lines = render.lines()
        assertEquals(1, lines.size)
        assertTrue("Render has an empty line") { lines[0].contains(some.name) }
    }

    @Test
    fun `PrettyValueGrid grid rendering with build`(){
        val text = "value_grid_text"
        val grid = buildGrid<TestValueGridRendering>(Grid.Grid1){
            useGrid(someClassGrid, TestValueGridRendering::some){
                buildRow { add(text) }
            }
        }
        val render = grid.render(this)
        render.output(enableOutput)
        val lines = render.lines()
        assertEquals(2, lines.size)
        assertTrue("Line from someClassGrid not rendered") { lines[0].contains(some.name) }
        assertTrue("Line from Grid.Grid1 not rendered") { lines[1].contains(text) }
    }
}