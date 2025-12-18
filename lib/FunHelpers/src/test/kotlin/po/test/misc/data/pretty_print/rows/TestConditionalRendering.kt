package po.test.misc.data.pretty_print.rows

import po.misc.data.output.output
import po.misc.data.pretty_print.PrettyValueGrid
import po.test.misc.data.pretty_print.setup.PrettyTestBase
import po.test.misc.data.pretty_print.setup.PrintableRecord
import po.test.misc.data.pretty_print.setup.PrintableRecordSubClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class TestConditionalRendering : PrettyTestBase() {


   private val record = createRecord()

    @Test
    fun `Row render can be controlled by dynamic conditions`(){

        val grid = buildGrid{
            buildRow {
                add(PrintableRecord::description)
            }
            buildRow(PrintableRecord::subClass){
                renderIf { it.description == "description" }
                add(PrintableRecordSubClass::subName)
            }
        }
        assertEquals(2, grid.renderMap.size)
        val render = grid.render(record)
        val valueGrid =  assertIs<PrettyValueGrid<PrintableRecord, PrintableRecordSubClass>>(grid.renderMap.elements[1])
        assertEquals(1, valueGrid.rows.size)
        assertEquals(false, valueGrid.rows[0].enabled)
        grid.journal.output()
    }

}