package po.test.misc.data.pretty_print.section

import org.junit.jupiter.api.Test
import po.misc.data.pretty_print.grid.buildPrettyGrid
import po.misc.data.pretty_print.parts.CellOptions
import po.misc.data.pretty_print.parts.CellRender
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.data.pretty_print.parts.RowRender
import po.misc.data.pretty_print.parts.RowPresets
import po.misc.data.pretty_print.section.PrettyTemplate
import po.misc.debugging.stack_tracer.StackFrameMeta
import po.misc.debugging.toFrameMeta
import po.misc.exceptions.TraceCallSite
import po.misc.exceptions.Tracer
import po.misc.exceptions.stack_trace.ExceptionTrace
import po.misc.exceptions.stack_trace.extractTrace
import po.misc.types.k_class.simpleOrAnon
import po.misc.types.token.TypeToken
import po.test.misc.data.pretty_print.setup.PrettyTestBase
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestPrettyTemplate : PrettyTestBase() {

    enum class Identification { Template1, Template2 }

    private val prettyGrid = buildPrettyGrid<StackFrameMeta> {
        buildRow {
            addCell("Header")
        }
        buildRow(RowPresets.Vertical) {
            addCell(StackFrameMeta::methodName)
            addCell(StackFrameMeta::lineNumber)
            addCell(StackFrameMeta::simpleClassName)
        }
    }
   // private val stackMetaTemplate = PrettyTemplate(prettyGrid)

    private fun getTraceMeta(): StackFrameMeta{
       return Tracer().firstTraceElement.toFrameMeta()
    }

    private fun getTrace(): ExceptionTrace{
        return Tracer().extractTrace(TraceCallSite(this::class.simpleOrAnon, null))
    }

    @Test
    fun `Templating work as expected`() {
        val stackMeta = getTraceMeta()
//        val render = stackMetaTemplate.render(stackMeta)
//        assertTrue { render.contains("getTraceMeta") }
    }

    @Test
    fun `Using grid as a template itself`() {
        val gridAsTemplate = buildPrettyGrid<PrintableRecord> {
            buildRow(RowPresets.Vertical) {
                addCell("Checking templating")
                addCell(PrintableRecord::name)
            }
        }
        val hostingGrid = buildPrettyGrid<PrintableRecord> {
            buildRow {
                addCell("Hosting grid")
            }
           // useTemplate(gridAsTemplate)
        }
        val record = createRecord()
        val hostingGridRender = hostingGrid.render(record)
        assertTrue { hostingGridRender.contains("Hosting grid") }
        assertTrue { hostingGridRender.contains("Checking templating") }
    }
}