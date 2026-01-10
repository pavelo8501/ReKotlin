package po.test.misc.data.pretty_print.rows

import org.junit.jupiter.api.Test
import po.misc.data.output.output
import po.misc.data.pretty_print.buildPrettyGrid
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.parts.template.RenderController
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

    private  val control = RenderController()
    @Test
    fun `Exclude row logic work as expected`() {
        val template = buildPrettyGrid<PrintableRecord> {
            buildRow() {
                orientation = Orientation.Vertical
                addAll(PrintableRecord::name, PrintableRecord::description)
            }
            buildRow(Row.Row1) {
                withControl(control)
                add(header2)
            }
        }
        control.enable = false
        val render = template.render(record)
        render.output(enableOutput)
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
                withControl(control)
                add(header2)
            }
        }
        control.enable = false
        val render = template.render(record)
        render.output(enableOutput)
        val lines = render.lines()
        assertEquals(2, lines.size)
        assertTrue { render.contains(record.name) }
        assertTrue { render.contains(header1) }
        assertFalse { render.contains(header2)  }
    }
}