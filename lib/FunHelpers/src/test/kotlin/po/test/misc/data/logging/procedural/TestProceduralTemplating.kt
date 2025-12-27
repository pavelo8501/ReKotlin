package po.test.misc.data.logging.procedural

import po.misc.data.logging.Loggable
import po.misc.data.logging.procedural.ProceduralEntry
import po.misc.data.logging.procedural.ProceduralFlow
import po.misc.data.logging.procedural.StepResult
import po.misc.data.output.output
import po.misc.data.pretty_print.PrettyValueGrid
import po.misc.data.styles.Colour
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestProceduralTemplating : ProceduralTestBase(){


    private val stepNameText = "Some step text"
    private val warnText1 = "Warning text 1"
    private val warnText2 = "Warning text 2"

    @Test
    fun `ProceduralEntry renders as expected`(){
        val record = ProceduralFlow.createEntry("Text")
        val render =  ProceduralEntry.template.render(record)
        render.output()
    }

    @Test
    fun `ProceduralEntry render warnings as expected`() {
        val record = ProceduralFlow.createEntry(stepNameText)
        val warning1 = warning("Warning 1", warnText1)
        val warning2 = warning("Warning 2", warnText2)
        record.logRecords.add(warning1)
        record.logRecords.add(warning2)

        assertIs<StepResult.Warning>(record.stepResult)
        assertNotNull(ProceduralEntry.template.renderPlan.renderables.lastOrNull()) { valueGrid ->
            assertIs<PrettyValueGrid<ProceduralEntry, Loggable>>(valueGrid)
            assertNotNull(valueGrid.rows.lastOrNull()) { row ->
                assertNotNull(row.computedCells.firstOrNull()) { computed ->
                    assertEquals("*", computed.cellOptions.keyText)
                }
            }
        }
        val render = ProceduralEntry.template.render(record)
        render.output()
        val lines = render.lines()
        assertEquals(3, lines.size)
        assertTrue { lines.first().contains(stepNameText) }
        assertTrue { lines.first().contains(Colour.YellowBright.code) }
        assertTrue { lines[1].contains(warnText1) && lines[2].contains(warnText2) }
    }

    @Test
    fun `ProceduralEntry correctly render result`() {

        val stringResult = "Some string"
        val nullResult : Any? = null
        val throwableResult = Throwable("Throwable as a result")
        val listResult = listOf("Element")
        val emptyListResult = emptyList<Any>()

        val record = ProceduralFlow.createEntry(stepNameText)

        val byString =  ProceduralFlow.toStepResult(stringResult)
        record.setResult(byString)
        var render = ProceduralEntry.template.render(record)
        assertIs<StepResult.OK>(record.stepResult)
        assertTrue { render.contains("OK") && render.contains(Colour.Green.code) }

        val byNullResult =  ProceduralFlow.toStepResult(nullResult)
        record.setResult(byNullResult)
        render = ProceduralEntry.template.render(record)
        assertIs<StepResult.Fail>(record.stepResult)
        assertTrue { render.contains("Fail") && render.contains(Colour.Red.code) }

        val byThrowable = ProceduralFlow.toStepResult(throwableResult)
        record.setResult(byThrowable)
        render = ProceduralEntry.template.render(record)
        assertIs<StepResult.Fail>(record.stepResult)
        assertTrue { render.contains("Fail") && render.contains(Colour.Red.code) }

        val byList =  ProceduralFlow.toStepResult(listResult)
        record.setResult(byList)
        render = ProceduralEntry.template.render(record)
        assertIs<StepResult.OK>(record.stepResult)
        assertTrue { render.contains("OK") && render.contains(Colour.Green.code) }

        val byEmptyList =  ProceduralFlow.toStepResult(emptyListResult)
        record.setResult(byEmptyList)
        render = ProceduralEntry.template.render(record)
        assertIs<StepResult.Fail>(record.stepResult)
        assertTrue { render.contains("Fail") && render.contains(Colour.Red.code) }

        render.output()
    }

}