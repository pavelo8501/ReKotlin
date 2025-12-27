package po.test.misc.data.pretty_print.rows

import org.junit.jupiter.api.Test
import po.misc.data.output.output
import po.misc.data.pretty_print.grid.buildPrettyGrid
import po.misc.data.pretty_print.parts.options.Orientation
import po.test.misc.data.pretty_print.setup.PrettyTestBase
import po.test.misc.data.pretty_print.setup.PrintableRecord
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestNamedRows  : PrettyTestBase(){


    private val printableRecord = PrintableRecord()
    private val header1: String = "Header_1"
    private val header2: String = "Header_2"
    private val header3: String = "Header_3"

    private val record = createRecord()



    @Test
    fun `Exclude row logic work as expected`() {
        val template = buildPrettyGrid<PrintableRecord> {
            buildRow() {
                orientation = Orientation.Vertical
                addAll(PrintableRecord::name, PrintableRecord::description)
            }
            buildRow(Row.Row1) {
                add(header2)
            }
        }
        val render = template.render(record)
        assertTrue { render.contains(record.name) }
        assertFalse { render.contains(header2)  }
    }

    @Test
    fun `RenderOnly logic work as expected`() {
        val template = buildPrettyGrid<PrintableRecord> {
            buildRow {
                orientation = Orientation.Vertical
                addAll(PrintableRecord::name, PrintableRecord::description)
            }
            buildRow(Row.Row1) {
                add(header1)
            }
            buildRow(Row.Row2) {
                add(header2)
            }
        }
        val render = template.render(record)
        assertTrue { render.contains(record.name) }
        assertTrue { render.contains(header1) }
        assertFalse { render.contains(header2)  }
        render.output()
    }

    @Test
    fun `RenderOnly list does not affect rows with no id`(){

        val grid = buildPrettyGrid<PrintableRecord>{
            headedRow(header1)
            buildRow(Row.Row1){
                add(header2)
            }
            buildRow(Row.Row2){
                add(header3)
            }
        }
        assertEquals(3, grid.rows.size)
        val render = grid.render(record)
        assertTrue { render.contains(header1) }
        assertFalse { render.contains(header2)  }
        assertTrue { render.contains(header3) }
    }
}