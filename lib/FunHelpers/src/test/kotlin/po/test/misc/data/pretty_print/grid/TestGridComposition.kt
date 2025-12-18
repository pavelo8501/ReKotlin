package po.test.misc.data.pretty_print.grid

import po.misc.data.pretty_print.PrettyValueGrid
import po.misc.data.pretty_print.grid.buildPrettyGrid
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.grid.buildPrettyListGrid
import po.test.misc.data.pretty_print.setup.PrettyTestBase
import po.test.misc.data.pretty_print.setup.PrintableElement
import po.test.misc.data.pretty_print.setup.PrintableRecord
import po.test.misc.data.pretty_print.setup.PrintableRecordSubClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class TestGridComposition : PrettyTestBase(){

    private val headerText: String = "Header"

    private val subGrid = buildPrettyGrid(PrintableRecord::subClass){
        buildRow {
            add(PrintableRecordSubClass::subName)
        }
    }
    private val elementsGrid = buildPrettyListGrid(PrintableRecord::elements){
        buildRow {
            add(PrintableElement::elementName)
        }
    }
    private val elementGrid = buildPrettyGrid<PrintableElement>{
        buildRow {
            add(headerText)
        }
        buildRow {
            add(PrintableElement::elementName)
        }
    }
    private val subClassGrid = buildPrettyGrid<PrintableRecordSubClass>{
        buildRow {
            add(PrintableRecordSubClass::subName)
        }
    }

    @Test
    fun `Grid accepts another  PrettyValueGrid as template if transition property provided`(){
        val grid = buildPrettyGrid<PrintableRecord> {
            buildRow {
                add(PrintableRecord::name)
            }
            useTemplate(subGrid, PrintableRecord::subClass)
            useTemplate(elementsGrid, PrintableRecord::elements)
        }

        assertNotNull(grid.renderMap.renderables.firstOrNull()) { renderBlock ->
            assertIs<PrettyRow<PrintableRecord>>(renderBlock)
        }
        assertNotNull(grid.renderMap.renderables.getOrNull(1)) { renderBlock ->
            assertIs<PrettyValueGrid<PrintableRecord, PrintableRecordSubClass>>(renderBlock)
            assertNotNull(renderBlock.singleLoader.propertyBacking)
        }
        assertNotNull(grid.renderMap.renderables.getOrNull(2)) { renderBlock ->
            assertIs<PrettyValueGrid<PrintableRecord, PrintableElement>>(renderBlock)
            assertNotNull(renderBlock.listLoader.propertyBacking)
        }
    }

    @Test
    fun `Grid accepts another  PrettyGrid as template if transition property provided`(){
        val grid = buildPrettyGrid<PrintableRecord> {
            useTemplate(subClassGrid, PrintableRecord::subClass)
        }
        assertNotNull(grid.renderMap.renderables.firstOrNull()) { renderBlock ->
            assertIs<PrettyValueGrid<PrintableRecord, PrintableRecordSubClass>>(renderBlock)
            assertEquals(PrintableRecordSubClass::class, renderBlock.type.kClass)
            assertNotNull(renderBlock.singleLoader.propertyBacking)
            assertEquals(1, renderBlock.rows.size)
        }
    }

    @Test
    fun `Grid as list style template`(){
        val grid = buildPrettyGrid<PrintableRecord> {
            useTemplate(elementGrid, PrintableRecord::elements)
        }
        assertNotNull(grid.renderMap.renderables.firstOrNull()) { renderBlock ->
            assertIs<PrettyValueGrid<PrintableRecord, PrintableElement>>(renderBlock)
            assertNotNull(renderBlock.listLoader.propertyBacking)
            assertEquals(2, renderBlock.rows.size)
        }
    }
}