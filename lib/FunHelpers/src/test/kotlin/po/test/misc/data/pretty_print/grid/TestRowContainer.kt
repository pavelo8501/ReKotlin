package po.test.misc.data.pretty_print.grid

import po.misc.data.pretty_print.cells.KeyedCell
import po.misc.data.pretty_print.grid.buildPrettyGrid
import po.misc.data.pretty_print.grid.buildPrettyGridList
import po.misc.data.pretty_print.rows.PrettyRow
import po.test.misc.data.pretty_print.setup.PrettyTestBase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestRowContainer : PrettyTestBase() {

    @Test
    fun `Row container's grid builder work as expected`() {
        val grid = buildPrettyGrid<PrintableRecord> {
            buildRow {
                addCell(PrintableRecord::name)
            }
        }
        assertNotNull(grid.rows.firstOrNull()) { firstRow ->
            val prettyRow = assertIs<PrettyRow<PrintableRecord>>(firstRow)
            assertEquals(PrintableRecord::class, firstRow.typeToken.kClass)
            assertNotNull(prettyRow.cells.firstOrNull()) { firstCell ->
                val keyedCell = assertIs<KeyedCell<PrintableRecord>>(firstCell)
                assertEquals(PrintableRecord::class, keyedCell.typeToken.kClass)
            }
        }
        assertEquals(1, grid.rows.size)
    }

    @Test
    fun `Row container's with receiver grid builder work as expected`(){
        val grid = buildPrettyGrid(PrintableRecord::subClass){
            buildRow {
                addCell(PrintableRecordSubClass::subName)
            }
        }
        assertNotNull(grid.rows.firstOrNull()){firstRow->
            val prettyRow = assertIs<PrettyRow<PrintableRecordSubClass>>(firstRow)
            assertEquals(PrintableRecordSubClass::class, prettyRow.typeToken.kClass)
            assertNotNull(prettyRow.cells.firstOrNull()){firstCell->
                val keyedCell = assertIs<KeyedCell<PrintableRecordSubClass>>(firstCell)
                assertEquals(PrintableRecordSubClass::class, keyedCell.typeToken.kClass)
            }
        }
        assertTrue{ grid.singleLoader.hasProperty }
    }

    @Test
    fun `Row container's with receiver grid builder correctly work with lists`(){
        val grid = buildPrettyGridList(PrintableRecord::elements){
            buildRow{
                addCell(PrintableElement::elementName)
            }
        }
        assertTrue{ grid.listLoader.hasProperty }
    }
}
