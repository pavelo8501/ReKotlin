package po.test.misc.data.pretty_print.grid

import org.junit.jupiter.api.Test
import po.misc.data.output.output
import po.misc.data.pretty_print.parts.Align
import po.misc.data.pretty_print.cells.KeyedCell
import po.misc.data.pretty_print.grid.buildPrettyGrid
import po.misc.data.pretty_print.grid.prettyGrid
import po.misc.data.pretty_print.parts.CellOptions
import po.misc.data.pretty_print.presets.RowPresets
import po.misc.data.styles.Colour
import po.misc.types.isNotNull
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestPrettyGrid {


    private class PrintableRecordSubClass(
        val subName: String = "PrintableRecordSubClass",
        val subComponent: String = "PrintableRecordSubClass component",
    )

    private class PrintableRecord(
        val name: String = "PersonalName",
        val component: String = "Component name ",
        val description: String = "Some description of the component",
        val subClass: PrintableRecordSubClass = PrintableRecordSubClass()
    )

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
            buildRow(RowPresets.VerticalRow) {
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
        val record = PrintableRecord()
        val rendered = prettyGrid.render(record)
        rendered.output()
    }

    @Test
    fun `Grid builder with context transition`() {

        val prettyGrid = buildPrettyGrid<PrintableRecord> {
            buildRow {
                addCell("Static")
            }
            buildRow(PrintableRecord::subClass, RowPresets.VerticalRow) {
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
        val record = PrintableRecord()
        val render = prettyGrid.render(record)
        render.output()

    }


}