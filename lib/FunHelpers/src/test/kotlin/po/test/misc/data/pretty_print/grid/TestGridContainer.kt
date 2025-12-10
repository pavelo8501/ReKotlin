package po.test.misc.data.pretty_print.grid

import po.misc.data.pretty_print.Templated
import po.misc.data.pretty_print.cells.KeyedCell
import po.misc.data.pretty_print.PrettyValueGrid
import po.misc.data.pretty_print.grid.buildPrettyGrid
import po.misc.data.pretty_print.grid.buildPrettyGridList
import po.misc.data.pretty_print.PrettyRow
import po.test.misc.data.pretty_print.setup.PrettyTestBase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestGridContainer : PrettyTestBase(), Templated {

    @Test
    fun `GridContainer's grid builder work as expected`() {
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
    fun `GridContainer's with receiver grid builder work as expected`(){
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
    fun `GridContainer's with receiver grid builder correctly work with lists`(){
        val grid = buildPrettyGridList(PrintableRecord::elements){
            buildRow{
                addCell(PrintableElement::elementName)
            }
        }
        assertTrue{ grid.listLoader.hasProperty }
    }

    @Test
    fun `GridContainer's build row correctly creates nested row with transition`(){
        val grid = buildPrettyGrid<PrintableRecord> {
            buildRow {
                addCell(PrintableRecord::name)
            }
            buildRow(PrintableRecord::subClass){
                addCell(PrintableRecordSubClass::subName)
            }
        }
        assertNotNull(grid.rows.firstOrNull()) { firstRow ->
            val prettyRow = assertIs<PrettyRow<PrintableRecord>>(firstRow)
            val firstCell = assertNotNull(prettyRow.cells.firstOrNull())
            assertIs<KeyedCell<PrintableRecord>>(firstCell)
            assertEquals(PrintableRecord::class, firstCell.typeToken.kClass)
        }
        assertEquals(1, grid.rows.size)
        assertNotNull(grid.renderBlocks.getOrNull(1)) { renderBlock ->
            val prettyGrid = assertIs<PrettyValueGrid<PrintableRecord, PrintableRecordSubClass>>(renderBlock)
            assertNotNull(prettyGrid.singleLoader.propertyBacking)
            assertNotNull(prettyGrid.rows.firstOrNull()){prettyRow->
                val firstCell = assertNotNull(prettyRow.cells.firstOrNull())
                assertIs<KeyedCell<PrintableRecordSubClass>>(firstCell)
                assertEquals(PrintableRecordSubClass::class, firstCell.typeToken.kClass)
            }
        }
    }

    @Test
    fun `GridContainer's buildRowList correctly creates nested row with transition`(){
        val grid = buildPrettyGrid<PrintableRecord> {
            buildRow {
                addCell(PrintableRecord::name)
            }
            buildListRow(PrintableRecord::elements){
                addCell(PrintableElement::elementName)
            }
        }
        assertNotNull(grid.rows.firstOrNull()) { firstRow ->
            val prettyRow = assertIs<PrettyRow<PrintableRecord>>(firstRow)
            val firstCell = assertNotNull(prettyRow.cells.firstOrNull())
            assertIs<KeyedCell<PrintableRecord>>(firstCell)
            assertEquals(PrintableRecord::class, firstCell.typeToken.kClass)
        }
        assertEquals(1, grid.rows.size)
        assertNotNull(grid.renderBlocks.getOrNull(1)) { renderBlock ->
            val prettyGrid = assertIs<PrettyValueGrid<PrintableRecord, PrintableElement>>(renderBlock)
            assertNotNull(prettyGrid.listLoader.propertyBacking)
            assertNotNull(prettyGrid.rows.firstOrNull()){prettyRow->
                val firstCell = assertNotNull(prettyRow.cells.firstOrNull())
                assertIs<KeyedCell<PrintableElement>>(firstCell)
                assertEquals(PrintableElement::class, firstCell.typeToken.kClass)
            }
        }
    }
}
