package po.test.misc.data.pretty_print.rows

import po.misc.data.pretty_print.cells.KeyedCell
import po.misc.data.pretty_print.cells.StaticCell
import po.misc.data.pretty_print.rows.buildPrettyRow
import po.test.misc.data.pretty_print.setup.PrettyTestBase
import po.test.misc.data.pretty_print.setup.PrintableRecord
import po.test.misc.data.pretty_print.setup.PrintableRecordSubClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class TestPrettyRowBuilder : PrettyTestBase() {

    private val headerText = "Header"

    @Test
    fun `Row container's grid builder correctly creates cells `() {
        val row = buildPrettyRow<PrintableRecord> {
            add(headerText)
            addAll(PrintableRecord::name, PrintableRecord::description)
        }
        assertEquals(3, row.cells.size)
    }

    @Test
    fun `buildPrettyRow correctly creates multiple cells`() {
        val row = buildPrettyRow<PrintableRecordSubClass>{
            add(headerText)
            addAll(PrintableRecordSubClass::subName, PrintableRecordSubClass::subComponent)
        }
        assertEquals(3, row.cells.size)
        assertIs<StaticCell>(row.cells.first())
        assertIs<KeyedCell<PrintableRecordSubClass>>(row.cells[1])
        assertIs<KeyedCell<PrintableRecordSubClass>>(row.cells[2])
    }


}