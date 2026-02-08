package po.test.misc.data.pretty_print.rows

import po.misc.data.PrettyPrint
import po.misc.data.output.output
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.cells.ComputedCell
import po.misc.data.pretty_print.cells.KeyedCell
import po.misc.data.pretty_print.cells.StaticCell
import po.misc.data.pretty_print.parts.options.CellPresets
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.buildPrettyRow
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.test.misc.data.pretty_print.setup.PrettyTestBase
import po.test.misc.data.pretty_print.setup.PrintableRecord
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
    val headerText1: String = "header_text_1"
    val auxText: String = "Aux_Text"

    @Test
    fun `Row renders  static cell + keyed cell as expected`(){
        val row = buildPrettyRow<PrintableRecord> {
            add(headerText1)
            val cell = add(PrintableRecord::name)
            cell.keyStyle
        }
        val render = row.render(record)
        row.cells.output()
        render.output(verbosity)
        assertTrue { render.contains(headerText1) }
        assertTrue { render.contains("Name") && render.contains(record.name) }
    }

    @Test
    fun `Row renders  static cell + keyed cell + computed cell as expected`(){
        val row = buildPrettyRow<PrintableRecord> {
            add(headerText1)
            add(PrintableRecord::name)
            add(PrintableRecord::component){
                it
            }
        }
        assertNotNull(row.cells.firstOrNull()){firstCell->
            assertIs<StaticCell>(firstCell)
        }
        assertNotNull(row.cells.getOrNull(1)) { secondCell ->
            assertIs<KeyedCell<PrintableRecord>>(secondCell)
        }
        assertNotNull(row.cells.getOrNull(2)) { thirdCell ->
            assertIs<ComputedCell<PrintableRecord, String>>(thirdCell)
            assertTrue(thirdCell.dataLoader.hasProperty)
        }
        val render =  row.render(record)
        render.output(verbosity)
        assertTrue("Static cells $headerText1 text missing") { render.contains(headerText1) }
        assertTrue("Property name not registered") { render.contains("Name") }
        assertTrue("Property value not registered") { render.contains(record.name) }
        assertTrue("Computed cell rendered value ${record.component} missing") { render.contains(record.component) }
    }

    @Test
    fun `Row renders vararg values, expecting rendered static + keyed cell + pretty cell with auxText value`(){
        val row = buildPrettyRow<PrintableRecord> {
            add(headerText1)
            add(PrintableRecord::name)
            addCell()
        }
        val render = row.renderAny(record, auxText)
        render.output(verbosity)
        assertTrue("Static cell not rendered") { render.contains(headerText1) }
        assertTrue("Keyed cell not rendered") { render.contains(record.name) }
        assertTrue("Pretty cell not rendered") { render.contains(auxText) }
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
    fun `Row orientation parameter applied correctly`(){
        val row = buildPrettyRow<PrintableRecord> {
            orientation = Orientation.Vertical
        }
        assertEquals(Orientation.Vertical, row.options.orientation)
    }

    @Test
    fun `Row rendering with builder Keyed cell + static cell`(){
        val row = buildRow {
            add(PrintableRecord::name)
            add("Static")
        }
        val render = row.render(record)
        render.output(verbosity)
        val lines = render.lines()
        assertEquals(1, lines.size)
        assertTrue { lines.first().contains("Static") }
        assertTrue { lines.first().contains(record.name) }

    }
}