package po.test.misc.data.pretty_print.section

import org.junit.jupiter.api.Test
import po.misc.data.pretty_print.grid.buildPrettyGrid
import po.misc.data.pretty_print.parts.CellOptions
import po.misc.data.pretty_print.parts.CellRender
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.data.pretty_print.parts.RowRender
import po.misc.data.pretty_print.presets.RowPresets
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
        buildRow(RowPresets.VerticalRow) {
            addCell(StackFrameMeta::methodName)
            addCell(StackFrameMeta::lineNumber)
            addCell(StackFrameMeta::simpleClassName)
        }
    }
    private val stackMetaTemplate = PrettyTemplate(TypeToken.create<StackFrameMeta>(), prettyGrid)

    private fun getTraceMeta(): StackFrameMeta{
       return Tracer().firstTraceElement.toFrameMeta()
    }

    private fun getTrace(): ExceptionTrace{
        return Tracer().extractTrace(TraceCallSite(this::class.simpleOrAnon, null))
    }

    @Test
    fun `Templating work as expected`() {
        val stackMeta = getTraceMeta()
        val render = stackMetaTemplate.render(stackMeta)
        assertTrue { render.contains("getTraceMeta") }
    }

    @Test
    fun `Templating rows render can be switched on or off by identification parameter`() {
        val gridForTemplate = buildPrettyGrid<PrintableRecord> {
            buildRow(RowPresets.VerticalRow) {
                addCell("Checking templating")
                addCell(PrintableRecord::name)
            }
        }
        val newGridTemplate = PrettyTemplate(TypeToken.create<PrintableRecord>(), gridForTemplate, Identification.Template1 )

        val hostingGrid = buildPrettyGrid<PrintableRecord> {
            buildRow(RowOptions(Identification.Template1)) {
                addCell("Hosting grid")
            }
            useTemplate(newGridTemplate)
        }
        val record = createRecord()
        var hostingGridRender = hostingGrid.render(record)

        assertTrue { hostingGridRender.contains("Hosting grid") }
        assertTrue { hostingGridRender.contains("Checking templating") }

        val renderOptions = RowRender(Identification.Template1, Identification.Template2)
        hostingGridRender = hostingGrid.render(record, renderOptions)
        assertTrue { hostingGridRender.contains("Hosting grid") }
        assertTrue { hostingGridRender.contains("Checking templating") }
    }

    @Test
    fun `Templating cell render control by id work as expected`() {
        val noIdText = "No id should be visible anyway"
        val cell1Text = "Cell with id Cell1"
        val cell2Text = "Cell with id Cell2"
        val grid = buildPrettyGrid<PrintableRecord> {
            buildRow(RowPresets.VerticalRow) {
                addCell(noIdText)
                addCell(cell1Text, CellOptions(CellTemplate.Cell1))
                addCell(cell2Text, CellOptions(CellTemplate.Cell2))
            }
        }
        val record = createRecord()
        var hostingGridRender = grid.render(record, CellRender(CellTemplate.Cell1))
        assertTrue { hostingGridRender.contains(noIdText) &&  hostingGridRender.contains(cell1Text)}
        assertFalse { hostingGridRender.contains(cell2Text) }

        hostingGridRender = grid.render(record, CellRender(CellTemplate.Cell2))
        assertTrue { hostingGridRender.contains(noIdText) &&  hostingGridRender.contains(cell2Text)}
        assertFalse { hostingGridRender.contains(cell1Text) }
    }

    @Test
    fun `Using grid as a template itself`() {
        val gridAsTemplate = buildPrettyGrid<PrintableRecord> {
            buildRow(RowPresets.VerticalRow) {
                addCell("Checking templating")
                addCell(PrintableRecord::name)
            }
        }
        val hostingGrid = buildPrettyGrid<PrintableRecord> {
            buildRow {
                addCell("Hosting grid")
            }
            useTemplate(gridAsTemplate)
        }
        val record = createRecord()
        val hostingGridRender = hostingGrid.render(record)
        assertTrue { hostingGridRender.contains("Hosting grid") }
        assertTrue { hostingGridRender.contains("Checking templating") }
    }

    @Test
    fun `Using template with switching receiver`() {

        val hostingGrid = buildPrettyGrid<PrintableRecord> {
            buildRow {
                addCell("Hosting grid")
            }
            useTemplate(stackMetaTemplate, ::getTraceMeta)
        }
        assertTrue {
            hostingGrid.prettyRows.size > 1
        }
        val record = createRecord()
        val render = hostingGrid.render(record)
        assertTrue { render.contains("Hosting grid") }
        assertTrue { render.contains("Simple class name") }
    }

    @Test
    fun `Using template with switching receiver of collection type`(){
        val metas = listOf(getTraceMeta(), getTraceMeta())
        val report = buildPrettyGrid<PrintableRecord>{
            buildRow {
                addCell("Call site trace report")
            }
            useTemplateForList(stackMetaTemplate){
                metas
            }
        }
        val render = report.render(createRecord())
        val getTraceMetaCount = render.split("getTraceMeta").size - 1
        assertEquals(2, getTraceMetaCount)
    }


}