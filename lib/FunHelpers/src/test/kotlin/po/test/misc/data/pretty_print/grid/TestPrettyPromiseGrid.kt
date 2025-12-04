package po.test.misc.data.pretty_print.grid

import org.junit.jupiter.api.Test
import po.misc.data.output.output
import po.misc.data.pretty_print.grid.PrettyPromiseGrid
import po.misc.data.pretty_print.grid.addHeadedRow
import po.misc.data.pretty_print.grid.buildPrettyGrid
import po.misc.data.pretty_print.parts.RowPresets
import po.misc.data.pretty_print.rows.PrettyRow
import po.test.misc.data.pretty_print.setup.PrettyTestBase
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestPrettyPromiseGrid : PrettyTestBase() {

    private val staticHeader1 = "Static header 1"
    private val staticFooter1 = "Static footer 1"

    private val staticSubHeader1 = "Static sub header 1"
    private val staticText1 = "Static first 1"

    private val staticSubHeader2 = "Static sub header 2"
    private val staticText2 = "Static second 1"

    private val template1 = buildPrettyGrid(PrintableRecord::elements) { list ->
        addHeadedRow(staticSubHeader1, RowPresets.HeadedVertical)
        buildRow(list){
            addCell(staticText1)
            addCell(PrintableElement::elementName)
        }
    }

    private val template2 = buildPrettyGrid(PrintableRecord::elements) { list ->
        addHeadedRow(staticSubHeader2)
        buildRow(list){
            addCell(staticText2)
            addCell(PrintableElement::elementName)
        }
    }

    @Test
    fun ` PrettyPromiseGrid construction`() {
        assertEquals(0, template1.prettyRows.size)
        val record = createRecord()
        val rowsBuilt = template1.buildRenderTemplate(record)
        assertEquals(2, rowsBuilt.size)
        assertEquals(2, template1.prettyRows.size)
        assertEquals(2, template1.renderBlocks.size)
        template1.prettyRows.forEachIndexed {index, row->
            assertIs<PrettyRow<*>>(row)
            when (index) {
                0 -> assertEquals(1, row.cells.size)
                1 -> assertEquals(2, row.cells.size)
                3 -> assertEquals(2, row.cells.size)
            }
        }
    }

    @Test
    fun ` PrettyPromiseGrid render`() {
        val record = createRecord()
        val rowsBuilt = template1.renderTemplate(record)
        assertTrue { rowsBuilt.contains(staticSubHeader1) && rowsBuilt.contains(staticText1) }
        assertTrue { rowsBuilt.contains(record.elements.first().elementName) }
        assertTrue { rowsBuilt.contains(record.elements.last().elementName) }
    }

    @Test
    fun ` PrettyPromiseGrid usage as a template part`(){

        val report = buildPrettyGrid<PrintableRecord> {
            addHeadedRow(staticHeader1)
            useTemplate(template1)
            addHeadedRow(staticFooter1)
        }
        assertEquals(2, report.prettyRows.size)
        assertNotNull(report.renderBlocks.getOrNull(1)){renderable->
            assertIs<PrettyPromiseGrid<PrintableRecord, PrintableElement>>(renderable)
        }
        val record = createRecord()
        val reportRender =  report.render(record)
        reportRender.output()
        assertTrue { reportRender.contains(staticHeader1) && reportRender.contains(staticFooter1) }
        assertTrue{ reportRender.contains(staticSubHeader1) && reportRender.contains(staticText1) }
    }

    @Test
    fun `Multiple grids compiled correctly`(){

        val report = buildPrettyGrid<PrintableRecord> {
            addHeadedRow(staticHeader1)
            useTemplate(template1)
            useTemplate(template2)
            addHeadedRow(staticFooter1)
        }

        assertEquals(2, report.prettyRows.size)
        val promiseGrids =  report.getRenderable<PrettyPromiseGrid<PrintableRecord, PrintableElement>>()
        assertEquals(2, promiseGrids.size)
        val lastGrid =  promiseGrids.last()
        assertEquals(0, lastGrid.prettyRows.size)
        val record = createRecord()
        val reportRender =  report.render(record)
        reportRender.output()
        assertEquals(2,  lastGrid.prettyRows.size)
    }
}