package po.test.misc.data.pretty_print.grid

import org.junit.jupiter.api.Test
import po.misc.data.output.output
import po.misc.data.pretty_print.parts.Align
import po.misc.data.pretty_print.cells.KeyedCell
import po.misc.data.pretty_print.grid.addHeadedRow
import po.misc.data.pretty_print.grid.buildHeadedRow
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

class TestPrettyGridExtensions : PrettyTestBase() {

    private val headerText1 = "FirstHeader"
    private val textRow1 = "Report of something(centered)"

    private val textRow2 = "Left aligned segment"

    @Test
    fun `Grid headed row shorthand extension`() {
        val prettyGrid = buildPrettyGrid<PrintableRecord> {
            addHeadedRow(headerText1)
            buildHeadedRow(textRow1){
                addCell(textRow2)
            }
            buildRow(RowPresets.Vertical) {
                addCell(PrintableRecord::name)
                addCell(PrintableRecord::component)
                addCell(PrintableRecord::description)
            }
        }
        assertEquals(3, prettyGrid.prettyRows.size)
        assertEquals(1, prettyGrid.prettyRows.first().cells.size)
        assertEquals(2, prettyGrid.prettyRows[1].cells.size, "One cell should be added automatically")
        assertEquals(3, prettyGrid.prettyRows.last().cells.size)
        val record = createRecord()
        val render = prettyGrid.render(record)
        assertTrue { render.contains(headerText1) && render.contains(textRow1) && render.contains(textRow2) }
    }

    @Test
    fun `Grid headed  extensions resolved to correct options`() {
        val prettyGrid = buildPrettyGrid<PrintableRecord> {
            addHeadedRow(headerText1, RowPresets.HeadedHorizontal)
        }
        assertNotNull(prettyGrid.prettyRows.firstOrNull()){row->
            assertNotNull(row.cells.firstOrNull()){cell->
                assertEquals(Align.CENTER, cell.options.alignment)
            }
        }
        prettyGrid.render(createRecord()).output()
    }
}