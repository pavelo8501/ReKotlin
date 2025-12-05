package po.test.misc.data.pretty_print.grid

import po.misc.data.pretty_print.grid.PrettyGrid
import po.misc.data.pretty_print.grid.PrettyValueGrid
import po.misc.data.pretty_print.grid.buildPrettyGrid
import po.misc.data.pretty_print.grid.buildPrettyGridList
import po.misc.data.pretty_print.rows.PrettyRow
import po.test.misc.data.pretty_print.setup.PrettyTestBase
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class TestGridComposition : PrettyTestBase(){

    private val subGrid = buildPrettyGrid(PrintableRecord::subClass){
        buildRow {
            addCell(PrintableRecordSubClass::subName)
        }
    }
    private val elementsGrid = buildPrettyGridList(PrintableRecord::elements){
        buildRow {
            addCell(PrintableElement::elementName)
        }
    }
    private val elementGrid = buildPrettyGrid<PrintableElement>{
        buildRow {
            addCell(PrintableElement::elementName)
        }
    }

    @Test
    fun `Grid accepts another  PrettyValueGrid as template if transition property provided`(){
        val grid = buildPrettyGrid<PrintableRecord> {
            buildRow {
                addCell(PrintableRecord::name)
            }
            useTemplate(subGrid, PrintableRecord::subClass)
            useListTemplate(elementsGrid, PrintableRecord::elements)
        }

        assertNotNull(grid.renderBlocks.firstOrNull()) { renderBlock ->
            assertIs<PrettyRow<PrintableRecord>>(renderBlock)
        }
        assertNotNull(grid.renderBlocks.getOrNull(1)) { renderBlock ->
            assertIs<PrettyValueGrid<PrintableRecord, PrintableRecordSubClass>>(renderBlock)
            assertNotNull(renderBlock.singleLoader.propertyBacking)
        }
        assertNotNull(grid.renderBlocks.getOrNull(2)) { renderBlock ->
            assertIs<PrettyValueGrid<PrintableRecord, PrintableElement>>(renderBlock)
            assertNotNull(renderBlock.listLoader.propertyBacking)
        }
    }

    @Test
    fun `Grid accepts another  PrettyGrid as template if transition property provided`(){

        val grid = buildPrettyGrid<PrintableRecord> {
            useTemplate(elementGrid, PrintableRecord::elements)
        }
        assertNotNull(grid.renderBlocks.firstOrNull()) { renderBlock ->
            assertIs<PrettyValueGrid<PrintableRecord, PrintableElement>>(renderBlock)
            assertNotNull(renderBlock.listLoader.propertyBacking)
        }
    }
}