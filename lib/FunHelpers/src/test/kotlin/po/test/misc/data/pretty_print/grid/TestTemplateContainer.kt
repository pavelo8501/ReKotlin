package po.test.misc.data.pretty_print.grid

import po.misc.data.output.output
import po.misc.data.pretty_print.cells.StaticCell
import po.misc.data.pretty_print.grid.PrettyGrid
import po.misc.data.pretty_print.grid.PrettyValueGrid
import po.misc.data.pretty_print.grid.addHeadedRow
import po.misc.data.pretty_print.grid.buildPrettyGrid
import po.misc.data.pretty_print.grid.buildRow
import po.misc.data.pretty_print.parts.Orientation
import po.misc.data.pretty_print.rows.PrettyRow
import po.misc.data.pretty_print.rows.buildPrettyRow
import po.misc.data.pretty_print.rows.buildRowContainer
import po.test.misc.data.pretty_print.setup.PrettyTestBase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestTemplateContainer : PrettyTestBase() {

    private val record = createRecord()

    fun getRecord(): PrintableRecord{
        return record
    }
   private  val subClassGrid: PrettyGrid<PrintableRecordSubClass> = buildPrettyGrid<PrintableRecordSubClass>(){
        buildRow(Row.SubTemplateRow) {
            addCells(PrintableRecordSubClass::subName, PrintableRecordSubClass::subComponent)
        }
    }
   private val elementGrid: PrettyGrid<PrintableElement> = buildPrettyGrid{
        buildRow(Row.SubTemplateRow) {
            addCell(PrintableElement::elementName)
        }
    }

    private val subClassRow = buildPrettyRow<PrintableRecordSubClass>(Row.SubTemplateRow) {
        addCells(PrintableRecordSubClass::subName, PrintableRecordSubClass::subComponent)
    }

    private val elementsRow = record.elements.buildRowContainer {
        addCell(PrintableElement::elementName)
    }
    @Test
    fun `Template is inserted as first if renderHere function not called`(){
        val grid = buildPrettyGrid<PrintableRecord>(Grid.Grid1){
            useTemplate(subClassGrid, PrintableRecord::subClass){
                buildRow {
                    addCell(headerText1)
                }
            }
        }

        assertNotNull(grid.renderBlocks.firstOrNull()){element->
           val valueGrid = assertIs<PrettyValueGrid<PrintableRecord, PrintableRecordSubClass>>(element)
           assertEquals(PrintableRecord::class, valueGrid.hostTypeToken.kClass)
           assertEquals(PrintableRecordSubClass::class, valueGrid.typeToken.kClass)

            assertNotNull(valueGrid.rows.firstOrNull()){row->
                val prettyRow = assertIs<PrettyRow<PrintableRecordSubClass>>(row)
                assertEquals(PrintableRecordSubClass::class, prettyRow.typeToken.kClass)
                assertEquals(2, prettyRow.cells.size)
                assertEquals(Row.SubTemplateRow, row.id)
            }
            assertNotNull(valueGrid.rows.getOrNull(1)){row->
                val prettyRow = assertIs<PrettyRow<PrintableRecordSubClass>>(row)
                assertEquals(PrintableRecordSubClass::class, prettyRow.typeToken.kClass)
                val cell =  assertNotNull(prettyRow.cells.firstOrNull())
                assertIs<StaticCell>(cell)
                assertEquals(headerText1, cell.text)
            }
        }
        assertEquals(1, grid.renderBlocks.size)
    }

    @Test
    fun `Template is inserted to a specific position`(){
        val grid = buildPrettyGrid<PrintableRecord>(Grid.Grid1){
            useTemplate(subClassGrid, PrintableRecord::subClass){
                addHeadedRow(headerText1, Row.Row1)
                renderHere()
                addHeadedRow(headerText2, Row.Row2)
            }
        }
        val template = assertIs<PrettyValueGrid<PrintableRecord, PrintableRecordSubClass>>(grid.renderBlocks.first())
        assertNotNull(template.rows.firstOrNull()){
               assertEquals(Row.Row1, it.id)
        }
        assertNotNull(template.rows.getOrNull(1)){row->
            val prettyRow = assertIs<PrettyRow<PrintableRecordSubClass>>(row)
            assertEquals(PrintableRecordSubClass::class, prettyRow.typeToken.kClass)
            assertEquals(2, prettyRow.cells.size)
            assertEquals(Row.SubTemplateRow, row.id)
        }
        assertNotNull(template.rows.getOrNull(2)){
            assertEquals(Row.Row2, it.id)
        }
    }

    @Test
    fun `List type template is created`(){
        val grid = buildPrettyGrid<PrintableRecord>(Grid.Grid1){
            useTemplateList(elementGrid, PrintableRecord::elements){
                addHeadedRow(headerText1, Row.Row1)
                renderHere()
            }
        }
        val template = assertIs<PrettyValueGrid<PrintableRecord, PrintableRecordSubClass>>(grid.renderBlocks.first())
        assertTrue(template.listLoader.hasProperty)
        assertNotNull(template.rows.getOrNull(1)){row->
            val prettyRow = assertIs<PrettyRow<PrintableElement>>(row)
            assertEquals(PrintableElement::class, prettyRow.typeToken.kClass)
        }
    }

    @Test
    fun `Template builder additional features work as expected`(){
        val grid = buildPrettyGrid<PrintableRecord>{
            useTemplateList(elementGrid, PrintableRecord::elements){
                useId(Grid.SubTemplateGrid)
                orientation = Orientation.Vertical
                exclude(Cell.Cell1, Cell.Cell2, Cell.Cell3, Cell.Cell4)
                addHeadedRow(headerText1, Row.Row1)
            }
        }
        val template = assertIs<PrettyValueGrid<PrintableRecord, PrintableRecordSubClass>>(grid.renderBlocks.first())
        assertEquals(Grid.SubTemplateGrid, template.id)
        assertEquals(Orientation.Vertical, template.options.orientation)
        assertEquals(4, template.options.excludeFromRenderList.size)
        assertEquals(Cell.Cell1, template.options.excludeFromRenderList.firstOrNull())
    }

    @Test
    fun `Template by prettyRow is created as expected`(){

        val grid = buildPrettyGrid<PrintableRecord>(Grid.Grid1){
            useTemplate(subClassRow, PrintableRecord::subClass){
                addHeadedRow(headerText1, Row.Row1)
            }
        }
        val template = assertIs<PrettyValueGrid<PrintableRecord, PrintableRecordSubClass>>(grid.renderBlocks.first())
        assertNotNull(template.rows.firstOrNull()){
            assertEquals(Row.SubTemplateRow, it.id)
        }
        assertNotNull(template.rows.getOrNull(1)){
            assertEquals(Row.Row1, it.id)
        }
    }
    @Test
    fun `Template by rowContainer is created as expected`(){
        val grid = buildPrettyGrid<PrintableRecord>(Grid.Grid1){
            useTemplate(elementsRow){
                addHeadedRow(headerText1, Row.Row1)
            }
        }
        assertNotNull(grid.gridMap.values.firstOrNull())
        grid.render(record).output()
    }
}