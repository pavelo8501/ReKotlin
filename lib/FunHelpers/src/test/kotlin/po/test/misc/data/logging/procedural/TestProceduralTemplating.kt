package po.test.misc.data.logging.procedural

import po.misc.data.logging.Loggable
import po.misc.data.logging.StructuredBase
import po.misc.data.logging.procedural.ProceduralEntry
import po.misc.data.logging.procedural.ProceduralFlow
import po.misc.data.logging.procedural.StepResult
import po.misc.data.output.output
import po.misc.data.pretty_print.PrettyValueGrid
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestProceduralTemplating : ProceduralTestBase(){

    @Test
    fun `ProceduralEntry renders as expected`(){
        val record = ProceduralFlow.createEntry("Text")
        val render =  ProceduralEntry.template.render(record)
        render.output()
    }


    @Test
    fun `ProceduralEntry render warnings as expected`() {

        val stepNameText = "Some step text"
        val warnText1 = "Warning text 1"
        val warnText2 = "Warning text 2"

        val record = ProceduralFlow.createEntry(stepNameText)
        val warning1 = warning("Warning 1", warnText1)
        val warning2 = warning("Warning 2", warnText2)
        record.logRecords.add(warning1)
        record.logRecords.add(warning2)

        assertIs<StepResult.Warning>(record.stepResult)
        assertNotNull(ProceduralEntry.template.renderMap.values.lastOrNull()) { valueGrid ->
            assertIs<PrettyValueGrid<ProceduralEntry, Loggable>>(valueGrid)
            assertNotNull(valueGrid.rows.lastOrNull()) { row ->
                assertNotNull(row.computedCells.firstOrNull()) { computed ->
                    assertEquals("*", computed.cellOptions.useForKey)
                }
            }
        }
        val render = ProceduralEntry.template.render(record)
        render.output()
        val lines = render.lines()
        assertEquals(3, lines.size)
        assertTrue { lines.first().contains(stepNameText) }

        assertTrue { lines[1].contains(warnText1) && lines[2].contains(warnText2) }
    }
}