package po.test.misc.data.pretty_print.cells

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import po.misc.data.output.output
import po.misc.data.pretty_print.cells.ComputedCell
import po.misc.data.pretty_print.cells.KeyedCell
import po.misc.data.pretty_print.parts.Orientation
import po.misc.data.pretty_print.rows.buildPrettyRow
import po.misc.data.styles.Colour
import po.misc.data.styles.TextStyle
import po.test.misc.data.pretty_print.setup.PrettyTestBase
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.text.contains

class TestComputedCell : PrettyTestBase() {

    @Test
    fun `Computed Cell default render is toString method of an instance`(){
        val row = buildPrettyRow<PrintableRecord> {
            addCell(PrintableRecord::subClass){subClass->
                subClass
            }
        }
        val cell = assertIs<ComputedCell<PrintableRecord, PrintableRecordSubClass>>(row.cells.first())
        val record = createRecord()
        val subClassToString = record.subClass.toString()
        val render = cell.render(record)
        assertTrue { render.contains(subClassToString) }
        assertFalse { render.contains(Colour.RESET.code)   }
    }

    @Test
    fun `Computed Cell respects usePlain modifier respectively printing formattedString of a class`(){
        val row = buildPrettyRow<PrintableRecord> {
            addCell(PrintableRecord::subClass){ subClass->
                usePlain = false
                subClass
            }
        }
        val cell = assertIs<ComputedCell<PrintableRecord, PrintableRecordSubClass>>(row.cells.first())
        val record = createRecord()
        val subClassFormattedString = record.subClass.formattedString
        val render = cell.render(record)
        assertTrue { render.contains(subClassFormattedString) }
        assertTrue { render.contains(Colour.Blue.code) }
    }

    @Test
    fun `Computed Cell passing itself to render would not create overflow`(){
        val row = buildPrettyRow<PrintableRecord> {
            addCell(PrintableRecord::subClass){
                this@addCell
            }
        }
        val cell = assertIs<ComputedCell<PrintableRecord, PrintableRecordSubClass>>(row.cells.first())
        assertDoesNotThrow {
            cell.render(createRecord())
        }
    }
}