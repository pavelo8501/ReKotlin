package po.test.misc.data.pretty_print.grid


import po.misc.data.pretty_print.cells.KeyedCell
import po.misc.data.pretty_print.cells.StaticCell
import po.misc.data.pretty_print.PrettyGrid
import po.misc.data.pretty_print.PrettyValueGrid
import po.misc.data.pretty_print.grid.addHeadedRow
import po.misc.data.pretty_print.grid.buildPrettyGrid
import po.misc.data.pretty_print.grid.buildRow
import po.misc.data.pretty_print.parts.Orientation
import po.misc.data.pretty_print.PrettyRow
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

    private val subClassGrid: PrettyGrid<PrintableRecordSubClass> = buildPrettyGrid<PrintableRecordSubClass>() {
        buildRow(Row.SubTemplateRow) {
            addCells(PrintableRecordSubClass::subName, PrintableRecordSubClass::subComponent)
        }
    }
    private val elementGrid: PrettyGrid<PrintableElement> = buildPrettyGrid {
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
    fun `Exactly one render block produced`() {

        val grid = buildPrettyGrid<PrintableRecord>(Grid.Grid1) {
            useTemplate(subClassGrid, PrintableRecord::subClass) {
                addHeadedRow(headerText1)
            }
        }
        assertEquals(1, grid.renderBlocks.size)
    }

    @Test
    fun `Template is inserted as first if renderHere function not called`() {
        val grid = buildPrettyGrid<PrintableRecord>(Grid.Grid1) {
            useTemplate(subClassGrid, PrintableRecord::subClass) {
                buildRow {
                    addCell(headerText1)
                }
            }
        }
        assertNotNull(grid.renderBlocks.firstOrNull()) { valueGrid ->
            assertIs<PrettyValueGrid<PrintableRecord, PrintableRecordSubClass>>(valueGrid)
            assertEquals(PrintableRecord::class, valueGrid.hostType.kClass)
            assertEquals(PrintableRecordSubClass::class, valueGrid.type.kClass)

            assertNotNull(valueGrid.rows.firstOrNull()) { prettyRow ->
               assertIs<PrettyRow<PrintableRecordSubClass>>(prettyRow)
               assertEquals(PrintableRecordSubClass::class, prettyRow.typeToken.kClass)
               assertEquals(2, prettyRow.cells.size)
               assertEquals(Row.SubTemplateRow, prettyRow.id)
            }
            assertNotNull(valueGrid.rows.getOrNull(1)) { prettyRow ->
                assertIs<PrettyRow<PrintableRecordSubClass>>(prettyRow)
                assertEquals(PrintableRecordSubClass::class, prettyRow.typeToken.kClass)
                assertNotNull(prettyRow.cells.firstOrNull()){cell->
                   assertIs<StaticCell>(cell)
                   assertEquals(headerText1, cell.text)
               }
            }
        }
        assertEquals(1, grid.renderBlocks.size)
    }

    @Test
    fun `Template is inserted to a specific position`() {
        val grid = buildPrettyGrid<PrintableRecord>(Grid.Grid1) {
            useTemplate(subClassGrid, PrintableRecord::subClass) {
                addHeadedRow(headerText1, Row.Row1)
                renderHere()
                addHeadedRow(headerText2, Row.Row2)
            }
        }
        val template = assertIs<PrettyValueGrid<PrintableRecord, PrintableRecordSubClass>>(grid.renderBlocks.first())
        assertNotNull(template.rows.firstOrNull()) {
            assertEquals(Row.Row1, it.id)
        }
        assertNotNull(template.rows.getOrNull(1)) { row ->
            assertIs<PrettyRow<PrintableRecordSubClass>>(row)
            assertEquals(PrintableRecordSubClass::class, row.typeToken.kClass)
            assertEquals(2, row.cells.size)
            assertEquals(Row.SubTemplateRow, row.id)
        }
        assertNotNull(template.rows.getOrNull(2)) {
            assertEquals(Row.Row2, it.id)
        }
    }

    @Test
    fun `List type template is created`() {
        val grid = buildPrettyGrid<PrintableRecord>(Grid.Grid1) {
            useListTemplate(elementGrid, PrintableRecord::elements) {
                addHeadedRow(headerText1, Row.Row1)
                renderHere()
            }
        }
        val template = assertIs<PrettyValueGrid<PrintableRecord, PrintableRecordSubClass>>(grid.renderBlocks.first())
        assertTrue(template.listLoader.hasProperty)
        assertNotNull(template.rows.getOrNull(1)) { row ->
            assertIs<PrettyRow<PrintableElement>>(row)
            assertEquals(PrintableElement::class, row.typeToken.kClass)
        }
    }

    @Test
    fun `Template builder additional features work as expected`() {
        val grid = buildPrettyGrid<PrintableRecord> {
            useListTemplate(elementGrid, PrintableRecord::elements) {
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
    fun `Template by prettyRow is created as expected`() {

        val grid = buildPrettyGrid<PrintableRecord>(Grid.Grid1) {
            useTemplate(subClassRow, PrintableRecord::subClass) {
                addHeadedRow(headerText1, Row.Row1)
            }
        }
        val template = assertIs<PrettyValueGrid<PrintableRecord, PrintableRecordSubClass>>(grid.renderBlocks.first())
        assertNotNull(template.rows.firstOrNull()) {
            assertEquals(Row.SubTemplateRow, it.id)
        }
        assertNotNull(template.rows.getOrNull(1)) {
            assertEquals(Row.Row1, it.id)
        }
    }

    @Test
    fun `Template by rowContainer is created as expected`() {

        val grid = buildPrettyGrid<PrintableRecord>(Grid.Grid1) {


            buildRow(PrintableRecord::elements){

            }

            useTemplate(elementsRow) {
                addHeadedRow(headerText1, Row.Row1)
            }
        }
        val template = assertIs<PrettyGrid<PrintableElement>>(grid.gridMap.values.firstOrNull())
        val firstRow = assertNotNull(template.rows.firstOrNull())
        val secondRow = assertNotNull(template.rows.getOrNull(1))
        assertIs<KeyedCell<PrintableElement>>(firstRow.cells.firstOrNull())
        assertEquals(1, firstRow.cells.size)
        assertIs<StaticCell>(secondRow.cells.firstOrNull())
        assertEquals(1, secondRow.cells.size)
        assertEquals(2, template.rows.size)
        assertEquals(1, elementsRow.cells.size)
    }
}