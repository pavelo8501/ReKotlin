package po.test.misc.data.pretty_print.grid

import org.junit.jupiter.api.Test
import po.misc.data.output.output
import po.misc.data.pretty_print.parts.Align
import po.misc.data.pretty_print.cells.KeyedCell
import po.misc.data.pretty_print.grid.buildPrettyGrid
import po.misc.data.pretty_print.parts.CellOptions
import po.misc.data.pretty_print.parts.CellRender
import po.misc.data.pretty_print.parts.Orientation
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.data.pretty_print.parts.RowRender
import po.misc.data.pretty_print.presets.PrettyPresets
import po.misc.data.pretty_print.parts.RowPresets
import po.misc.data.styles.Colour
import po.test.misc.data.pretty_print.setup.PrettyTestBase
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestPrettyGrid : PrettyTestBase() {

    @Test
    fun `Grid builder function usage`() {

        val textRow1 = "Report of something(centered)"
        val textRow2 = "Left aligned segment"

        val prettyGrid = buildPrettyGrid<PrintableRecord> {
            buildRow {
                addCell(textRow1, CellOptions(Align.CENTER, Colour.Blue))
            }
            buildRow {
                addCell(textRow2)
            }
            buildRow(RowPresets.Vertical) {
                addCell(PrintableRecord::name)
                addCell(PrintableRecord::component)
                addCell(PrintableRecord::description)
            }
        }
        assertEquals(3, prettyGrid.prettyRows.size)
        assertNotNull(prettyGrid.prettyRows.firstOrNull()) { row ->
            assertEquals(1, row.cells.size)
            val cell = row.cells.first()
            assertEquals(Align.CENTER, cell.options.alignment)
            assertEquals(Colour.Blue, cell.options.styleOptions.colour)
        }

        assertNotNull(prettyGrid.prettyRows[1]) { secondRow ->
            assertEquals(1, secondRow.cells.size)
        }
        assertNotNull(prettyGrid.prettyRows[2]) { keyedCellRow ->
            assertEquals(3, keyedCellRow.cells.size)
            val keyedCell = keyedCellRow.cells.first()
            assertIs<KeyedCell>(keyedCell)
            assertEquals(Colour.GreenBright, keyedCell.keyedOptions.styleOptions.colour)
        }
    }

    @Test
    fun `Grid builder with context transition`() {
        val prettyGrid = buildPrettyGrid<PrintableRecord> {
            buildRow {
                addCell("Static")
            }
            buildRow(PrintableRecord::subClass, RowPresets.Vertical) {
                addCell(PrintableRecordSubClass::subName)
                addCell(PrintableRecordSubClass::subComponent)
            }
        }
        assertEquals(2, prettyGrid.prettyRows.size)
        assertNotNull(prettyGrid.prettyRows.lastOrNull()){transitionRow->
            assertEquals(2, transitionRow.cells.size)
            assertNotNull(transitionRow.cells.lastOrNull()){cell->
                assertIs<KeyedCell>(cell)
                assertNotNull(cell.property)
            }
        }
    }

    @Test
    fun `PrettyGrid  with context switch`(){
        val prettyGrid = buildPrettyGrid<PrintableRecord> {
            buildRow {
                addCell("Static")
            }
            buildRows(PrintableRecord::elements){
                addCell(PrintableElement::elementName)
            }
        }
        assertEquals(2, prettyGrid.prettyRows.size)
        assertEquals(1, prettyGrid.prettyRows.last().cells.size)
        val record = createRecord()
        val render =  prettyGrid.render(record)
        render.output()

    }

    @Test
    fun `Using specific template id to control render`(){

        val cell1Text = "Static cell 1 on first row"
        val cell2Text = "Second Static cell on first row"
        val cell3Text = "First Static cell on second row"

        var prettyGrid = buildPrettyGrid<PrintableRecord> {
            buildRow(RowOptions(Orientation.Horizontal, id = Template.Template1)) {
                addCell(cell1Text)
                addCell(cell2Text)
            }
            buildRow(RowOptions(Orientation.Horizontal, id = Template.Template2)){
                addCell(cell3Text)
            }
        }
        val record = createRecord()
        var render = prettyGrid.render(record, RowRender(Template.Template1))
        assertTrue {  render.contains(cell1Text) && render.contains(cell2Text) }
        assertFalse { render.contains(cell3Text) }

        prettyGrid = buildPrettyGrid<PrintableRecord> {
            buildRow(RowOptions(Orientation.Horizontal, id = Template.Template1)) {
                addCell(cell1Text)
                addCell(cell2Text, CellOptions(CellTemplate.Cell2))
            }
            buildRow(RowOptions(Orientation.Horizontal, id = Template.Template2)){
                addCell(cell3Text)
            }
        }
        render = prettyGrid.render(record, RowRender(Template.Template1, CellTemplate.Cell2))

        assertTrue {  render.contains(cell1Text) && render.contains(cell2Text) }
        assertFalse { render.contains(cell3Text) }

        render = prettyGrid.render(record, RowRender(Template.Template1))
        assertTrue {  render.contains(cell1Text) }
        assertFalse { render.contains(cell2Text) && render.contains(cell3Text) }
    }
}