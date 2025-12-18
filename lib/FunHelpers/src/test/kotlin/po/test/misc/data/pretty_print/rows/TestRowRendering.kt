package po.test.misc.data.pretty_print.rows

import po.misc.data.PrettyPrint
import po.misc.data.output.output
import po.misc.data.pretty_print.cells.ComputedCell
import po.misc.data.pretty_print.cells.KeyedCell
import po.misc.data.pretty_print.cells.StaticCell
import po.misc.data.pretty_print.parts.CellPresets
import po.misc.data.pretty_print.parts.Orientation
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.data.pretty_print.rows.buildPrettyRow
import po.misc.data.pretty_print.rows.buildRowForContext
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.test.misc.data.pretty_print.setup.PrettyTestBase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class TestRowRendering : PrettyTestBase(){

    private class SomeClass(val text: String = "SomeText", val value: Int = 10): PrettyPrint{
        private val completeTex = "${text}_${value}"
        override val formattedString: String get() = completeTex.colorize(Colour.Green)
        override fun toString(): String = completeTex
    }

    private val record = createRecord()

    @Test
    fun `Row renders keyed cells`(){

        val row = record.buildRowForContext{
            addAll(PrintableRecord::name, PrintableRecord::description, PrintableRecord::component)
        }
        val render = row.render(record)
        render.output()
    }

    @Test
    fun `Row renders  static cell + keyed cell as expected`(){
        val row = buildPrettyRow<PrintableRecord> {
            add(headerText1)
            add(PrintableRecord::name)
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
        assertTrue { render.contains(headerText1) }
        assertTrue { render.contains("Name") && render.contains(record.name) }
    }

    @Test
    fun `Row renders  static cell + keyed cell + computed cell as expected`(){
        val row = buildPrettyRow<PrintableRecord> {
            add(headerText1)
            add(PrintableRecord::name)
            computed(PrintableRecord::component){
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
        assertTrue { render.contains(headerText1) }
        assertTrue { render.contains("Name") && render.contains(record.name) }
        assertTrue { render.contains("Component name_Computed") && render.contains(record.name) }
    }

    @Test
    fun `Builder correctly distinguish static cells from pretty cells`(){
        val row = buildPrettyRow<PrintableRecord> {
            add(headerText1)
            add(PrintableRecord::name)
            addCell()
        }
        val record = createRecord()
        val someClass = SomeClass()
        val render = row.renderAny(record, someClass)
        assertTrue { render.contains(headerText1) }
        assertTrue { render.contains("Name") && render.contains(record.name) }
        assertTrue { render.contains("SomeText_10") }
    }

    @Test
    fun `Multiple cell builder correctly apply options`(){

        val keyStyle = CellPresets.Property.keyStyle
        val valueStyle = CellPresets.Property.style

        val row = buildPrettyRow<PrintableRecord> {
            addAll(PrintableRecord::name, PrintableRecord::description)
        }
        val render = row.render(record)
        render.output()
        assertEquals(2, row.cells.size)
        assertNotNull(row.cells.firstOrNull()){cell->
            assertIs<KeyedCell<PrintableRecord>>(cell)
            assertEquals(keyStyle.textStyle, cell.cellOptions.keyStyle.textStyle)
            assertEquals(keyStyle.colour, cell.cellOptions.keyStyle.colour)
            assertEquals(valueStyle.textStyle, cell.cellOptions.style.textStyle)
            assertEquals(valueStyle.colour, cell.cellOptions.style.colour)

        }
        assertNotNull(row.cells.getOrNull(1)){cell->
            assertIs<KeyedCell<PrintableRecord>>(cell)
            assertEquals(keyStyle.textStyle, cell.cellOptions.keyStyle.textStyle)
            assertEquals(keyStyle.colour, cell.cellOptions.keyStyle.colour)
            assertEquals(valueStyle.textStyle, cell.cellOptions.style.textStyle)
            assertEquals(valueStyle.colour, cell.cellOptions.style.colour)
        }
    }

    @Test
    fun `Multiple cell orientation parameter applied correctly`(){

        val row = buildPrettyRow<PrintableRecord> {
            orientation = Orientation.Vertical
            addAll(PrintableRecord::name, PrintableRecord::description)
            beforeRowRender {

            }
        }
        var usedOptions: RowOptions? = null
        row.beforeRowRender{
            usedOptions = it.usedOptions
        }
        assertEquals(Orientation.Vertical, row.options.orientation)
        val render = row.render(record)
        assertNotNull(usedOptions)
        assertEquals(Orientation.Vertical, usedOptions.orientation)
    }


}