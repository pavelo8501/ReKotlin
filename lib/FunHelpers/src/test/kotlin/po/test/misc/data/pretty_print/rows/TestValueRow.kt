package po.test.misc.data.pretty_print.rows

import po.misc.data.output.output
import po.misc.data.pretty_print.PrettyValueGrid
import po.misc.data.pretty_print.PrettyValueRow
import po.misc.data.pretty_print.cells.KeyedCell
import po.misc.data.pretty_print.buildPrettyGrid
import po.test.misc.data.pretty_print.setup.PrettyTestBase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestValueRow : PrettyTestBase (){
    private val report = ReportClass(entryCount =  4)

    @Test
    fun `Type alignment`(){
        val template = buildPrettyGrid<ReportClass> {
            buildListGrid(ReportClass::entries) {
                buildRow(ReportEntry::record) {
                    add(ReportRecord::text)
                }
            }
        }
        assertEquals(ReportClass::class, template.receiverType.kClass)
        assertEquals(ReportClass::class, template.sourceType.kClass)
        assertNotNull(template.renderPlan[PrettyValueGrid].firstOrNull()){valueGrid->
            assertEquals(ReportClass::class, valueGrid.receiverType.kClass)
            assertEquals(ReportEntry::class, valueGrid.sourceType.kClass)
            assertNotNull(valueGrid.renderPlan[PrettyValueRow].firstOrNull()){valueRow->
                assertEquals(ReportEntry::class, valueRow.receiverType.kClass)
                assertEquals(ReportRecord::class, valueRow.sourceType.kClass)
                assertNotNull(valueRow.cells.firstOrNull()){cell->
                    assertIs<KeyedCell<ReportRecord>>(cell)
                    assertEquals(ReportRecord::class, cell.sourceType.kClass)
                }
            }
        }
    }

    @Test
    fun `Value row resolves T to V as expected`(){
        val template = buildPrettyGrid<ReportClass> {
            buildRow(ReportClass::record) {
                add(ReportRecord::text)
            }
        }
        val record = report.record
        assertNotNull(template.renderPlan[PrettyValueRow].firstOrNull())
        val render = template.render(report)
        render.output(verbosity)
        assertTrue("Record of report was not rendered"){ render.contains(record.text) }
    }


    @Test
    fun `Value row multi row render as expected`(){
        val grid = buildPrettyGrid<ReportClass> {
            buildListGrid(ReportClass::entries) {
                buildRow(ReportEntry::record){
                    add(ReportRecord::text)
                    add(ReportRecord::date)
                }
            }
        }
        val render = grid.render(report)
        render.output(verbosity)
    }
}