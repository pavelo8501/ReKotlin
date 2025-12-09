package po.test.misc.data.pretty_print.rows

import org.junit.jupiter.api.Test
import po.misc.collections.asList
import po.misc.data.output.output
import po.misc.data.pretty_print.Templated
import po.misc.data.pretty_print.grid.addHeadedRow
import po.misc.data.pretty_print.grid.buildGridForContext
import po.misc.data.pretty_print.grid.buildPrettyGrid
import po.misc.data.pretty_print.grid.buildRow
import po.misc.data.pretty_print.parts.Orientation
import po.misc.data.pretty_print.parts.RowOptions
import po.test.misc.data.pretty_print.setup.PrettyTestBase
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestNamedRows  : PrettyTestBase(), Templated {


    private val printableRecord = PrintableRecord()
    private val header1: String = "Header_1"
    private val header2: String = "Header_2"
    private val header3: String = "Header_3"

    private val record = createRecord()

    @Test
    fun `Named rows can be excluded from render`(){
        var usedOptions: RowOptions? = null
        val grid = printableRecord.buildGridForContext {
            buildRow(RowOptions(Row.Row1, Orientation.Horizontal)) {
                beforeRowRender {
                    usedOptions = it.usedOptions
                }
                addCell(header1)
            }
            buildRow(RowOptions(Row.Row2)) {
                addCell(header2)
            }
        }
        assertNotNull(grid.rows.firstOrNull()){row->
            assertEquals(Row.Row1, row.options.rowId)
            assertEquals(0, row.options.renderOnlyList.size)
        }
        assertNotNull(grid.rows.getOrNull(1)){row->
            assertEquals(Row.Row2, row.options.rowId)
            assertEquals(0, row.options.renderOnlyList.size)
        }
        val render = grid.render(printableRecord){
            exclude(Row.Row2)
        }
        assertNotNull(usedOptions){
            assertEquals(1, it.excludeFromRenderList.size)
        }
        assertTrue { render.contains(header1) }
        assertFalse { render.contains(header2) }
    }

    @Test
    fun `Named grids-templates can be excluded from render`(){
        val headerGrid = buildPrettyGrid<PrintableRecord>(RowOptions(Grid.Grid1)){
            addHeadedRow(header1)
        }
        val recordGrid = buildPrettyGrid<PrintableRecord>(RowOptions(Grid.Grid2)){
            buildRow {
                addCell(PrintableRecord::name)
            }
        }
        val grid = buildGridForContext {
            useTemplate(headerGrid){ printableRecord }
            useTemplate(recordGrid){ printableRecord }
        }
        val render = grid.render(this){
            exclude(Grid.Grid1)
        }
        assertNotNull(grid.renderBlocks.firstOrNull()){grid->
            assertEquals(Grid.Grid1, grid.id)
        }
        assertNotNull(grid.renderBlocks.getOrNull(1)){grid->
            assertEquals(Grid.Grid2, grid.id)
        }
        assertTrue { render.contains(printableRecord.name) }
        assertFalse { render.contains(header1) }
    }

    @Test
    fun `Exclude row logic work as expected`() {
        val template = buildPrettyGrid<PrintableRecord> {
            buildRow(Orientation.Vertical) {
                addCells(PrintableRecord::name, PrintableRecord::description)
            }
            buildRow(Row.Row1) {
                addCell(header2)
            }
        }
        val render = template.render(record) {
            exclude(Row.Row1)
        }
        assertTrue { render.contains(record.name) }
        assertFalse { render.contains(header2)  }
    }

    @Test
    fun `RenderOnly logic work as expected`() {
        val template = buildPrettyGrid<PrintableRecord> {
            buildRow(Orientation.Vertical) {
                addCells(PrintableRecord::name, PrintableRecord::description)
            }
            buildRow(Row.Row1) {
                addCell(header1)
            }
            buildRow(Row.Row2) {
                addCell(header2)
            }
        }
        val render = template.render(record) {
            renderOnly(Row.Row1.asList())
        }
        assertTrue { render.contains(record.name) }
        assertTrue { render.contains(header1) }
        assertFalse { render.contains(header2)  }
        render.output()
    }

    @Test
    fun `RenderOnly list does not affect rows with no id`(){

        val grid = buildPrettyGrid<PrintableRecord>{
            addHeadedRow(header1)
            buildRow{
                rowId = Row.Row1
                addCell(header2)
            }
            buildRow{
                rowId = Row.Row2
                addCell(header3)
            }
        }
        assertEquals(3, grid.rows.size)
        val render = grid.render(record){
            renderOnly(Row.Row2)
        }
        assertTrue { render.contains(header1) }
        assertFalse { render.contains(header2)  }
        assertTrue { render.contains(header3) }
    }
}