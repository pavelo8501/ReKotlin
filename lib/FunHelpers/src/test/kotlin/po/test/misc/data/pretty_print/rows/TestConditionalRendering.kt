package po.test.misc.data.pretty_print.rows

import po.misc.data.output.output
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.PrettyValueGrid
import po.test.misc.data.pretty_print.setup.PrettyTestBase
import po.test.misc.data.pretty_print.setup.PrintableRecord
import po.test.misc.data.pretty_print.setup.PrintableRecordSubClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class TestConditionalRendering : PrettyTestBase() {


   private val record = createRecord()

    @Test
    fun `Row render can be controlled by dynamic conditions`(){



        val grid = buildGrid{
            buildRow {
                add(PrintableRecord::description)
            }
            buildRow(PrintableRecord::subClass){

                val someValue: String = ""

                renderIf { it.description == "description" }
                add(PrintableRecordSubClass::subName)
            }
        }




        assertEquals(1, grid.renderPlan[PrettyRow::class].size)
        assertEquals(1, grid.renderPlan[PrettyValueGrid::class].size)
        val valueGrid = grid.renderPlan[PrettyValueGrid::class].first()

        assertEquals(1, valueGrid.rows.size)
        assertEquals(false, valueGrid.rows[0].enabled)
        grid.journal.output()
    }

}