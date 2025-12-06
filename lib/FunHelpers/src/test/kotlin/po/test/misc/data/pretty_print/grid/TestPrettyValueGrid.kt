package po.test.misc.data.pretty_print.grid

import org.junit.jupiter.api.assertDoesNotThrow
import po.misc.data.pretty_print.grid.buildPrettyGrid
import po.misc.data.pretty_print.grid.buildPrettyGridList
import po.test.misc.data.pretty_print.setup.PrettyTestBase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestPrettyValueGrid : PrettyTestBase(){

    @Test
    fun `On render PrettyValueGrid always resolves value`(){

        val grid = buildPrettyGrid(PrintableRecord::subClass){
            buildRow {
                addCell(PrintableRecordSubClass::subName)
            }
        }
        val record = createRecord()
        val subRecord = record.subClass
        var resolvedValue: PrintableRecordSubClass? = null

        assertDoesNotThrow {
            grid.render(record){
                resolvedValue = it
            }
        }
        assertNotNull(resolvedValue){
            assertEquals(subRecord.subName, it.subName)
        }
        val grid2 = buildPrettyGridList(PrintableRecord::elements){
            buildRow {
                addCell(PrintableElement::elementName)
            }
        }
        var resolvedElement: PrintableElement? = null
        assertDoesNotThrow{
            grid2.render(record){
                resolvedElement = it
            }
        }
        assertNotNull(resolvedElement){
            assertEquals(record.elements.last().elementName, it.elementName)
        }
    }



}