package po.test.misc.data.pretty_print.rows

import po.misc.data.PrettyPrint
import po.misc.data.output.output
import po.misc.data.pretty_print.cells.ComputedCell
import po.misc.data.pretty_print.cells.KeyedCell
import po.misc.data.pretty_print.cells.StaticCell
import po.misc.data.pretty_print.rows.buildPrettyRow
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.test.misc.data.pretty_print.setup.PrettyTestBase
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class TestRowRendering : PrettyTestBase(){

    private class SomeClass(
        val text: String = "SomeText",
        val value: Int = 10
    ): PrettyPrint{
        private val completeTex = "${text}_${value}"
        override val formattedString: String get() = completeTex.colorize(Colour.Green)
        override fun toString(): String = completeTex
    }

    private val headerText = "Header"

    @Test
    fun `Row renders  static cell + keyed cell as expected`(){
        val row = buildPrettyRow<PrintableRecord> {
            addCell(headerText)
            addCell(PrintableRecord::name)
        }
        assertNotNull(row.cells.firstOrNull()){firstCell->
            assertIs<StaticCell>(firstCell)
            assertNotNull(firstCell.row){firstCellRow->
                assertSame(row, firstCellRow)
            }
        }
        assertNotNull(row.cells.getOrNull(1)) { secondCell ->
            assertIs<KeyedCell<PrintableRecord>>(secondCell)
            assertNotNull(secondCell.row){firstCellRow->
                assertSame(row, firstCellRow)
            }
        }
        val record = createRecord()
        val render =  row.render(record)
        assertTrue { render.contains(headerText) }
        assertTrue { render.contains("Name") && render.contains(record.name) }
        render.output()
    }

    @Test
    fun `Row renders  static cell + keyed cell + computed cell as expected`(){

        val row = buildPrettyRow<PrintableRecord> {
            addCell(headerText)
            addCell(PrintableRecord::name)
            addCell(PrintableRecord::component){
                "${it}_Computed"
            }
        }
        assertNotNull(row.cells.firstOrNull()){firstCell->
            assertIs<StaticCell>(firstCell)
            assertNotNull(firstCell.row){firstCellRow->
                assertSame(row, firstCellRow)
            }
        }
        assertNotNull(row.cells.getOrNull(1)) { secondCell ->
            assertIs<KeyedCell<PrintableRecord>>(secondCell)
            assertNotNull(secondCell.row){firstCellRow->
                assertSame(row, firstCellRow)
            }
        }
        assertNotNull(row.cells.getOrNull(2)) { thirdCell ->
            assertIs<ComputedCell<PrintableRecord, String>>(thirdCell)
            assertTrue(thirdCell.singleLoader.hasProperty)
            assertNotNull(thirdCell.row){firstCellRow->
                assertSame(row, firstCellRow)
            }
        }
        val record = createRecord()
        val render =  row.render(record)
        assertTrue { render.contains(headerText) }
        assertTrue { render.contains("Name") && render.contains(record.name) }
        assertTrue { render.contains("Component name_Computed") && render.contains(record.name) }
    }

    @Test
    fun `Builder correctly distinguish static cells from pretty cells`(){
        val row = buildPrettyRow<PrintableRecord> {
            addCell(headerText)
            addCell(PrintableRecord::name)
            addCell()
        }
        val record = createRecord()
        val someClass = SomeClass()
        val render = row.renderAny(record, someClass)
        assertTrue { render.contains(headerText) }
        assertTrue { render.contains("Name") && render.contains(record.name) }
        assertTrue { render.contains("SomeText_10") }
    }

}