package po.test.misc.data.pretty_print.grid

import po.misc.data.pretty_print.grid.PrettyGrid
import po.misc.data.pretty_print.grid.PrettyValueGrid
import po.misc.data.pretty_print.grid.addHeadedRow
import po.misc.data.pretty_print.grid.buildPrettyGrid
import po.misc.data.pretty_print.grid.buildPrettyGridList
import po.misc.data.pretty_print.parts.GridSource
import po.misc.data.pretty_print.rows.PrettyRow
import po.test.misc.data.pretty_print.setup.PrettyTestBase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class TestGridComposition : PrettyTestBase(){

    private val headerText: String = "Header"
    private val testGridRenderingString = "TestGridRendering"
    private val testGridInt = 10

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
            addCell(headerText)
        }
        buildRow {
            addCell(PrintableElement::elementName)
        }
    }
    private val subClassGrid = buildPrettyGrid<PrintableRecordSubClass>{
        buildRow {
            addCell(PrintableRecordSubClass::subName)
        }
    }

    private val thisGrid = buildPrettyGrid<TestGridComposition>{
        addHeadedRow(headerText)
        buildRow {
            addCell(TestGridComposition::testGridRenderingString)
            addCell(TestGridComposition::testGridInt)
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
            useTemplate(subClassGrid, PrintableRecord::subClass)
        }
        assertNotNull(grid.renderBlocks.firstOrNull()) { renderBlock ->
            assertIs<PrettyValueGrid<PrintableRecord, PrintableRecordSubClass>>(renderBlock)
            assertEquals(PrintableRecordSubClass::class, renderBlock.type.kClass)
            assertNotNull(renderBlock.singleLoader.propertyBacking)
            assertEquals(1, renderBlock.rows.size)
        }
    }

    @Test
    fun `Grid as list style template`(){
        val grid = buildPrettyGrid<PrintableRecord> {
            useListTemplate(elementGrid, PrintableRecord::elements)
        }
        assertNotNull(grid.renderBlocks.firstOrNull()) { renderBlock ->
            assertIs<PrettyValueGrid<PrintableRecord, PrintableElement>>(renderBlock)
            assertNotNull(renderBlock.listLoader.propertyBacking)
            assertEquals(2, renderBlock.rows.size)
        }
    }

    @Test
    fun `Foreign grid rendered as expected`(){
        val grid = buildPrettyGrid<PrintableRecord>{
            buildRow {
                addCell(PrintableRecord::name)
            }
            useTemplate(thisGrid){
                this@TestGridComposition
            }
        }
        assertNotNull(grid.gridMap.entries.firstOrNull()){usedGridEntry->
            assertEquals(GridSource.Grid, usedGridEntry.key.source)
            assertEquals(1, usedGridEntry.key.order)
            val testGrid = assertIs<PrettyGrid<TestGridComposition>>(usedGridEntry.value)
            assertEquals(TestGridComposition::class, testGrid.type.kClass)
            assertEquals(2,  testGrid.size)
        }
    }

}