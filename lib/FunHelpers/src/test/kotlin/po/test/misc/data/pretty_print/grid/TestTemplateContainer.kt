package po.test.misc.data.pretty_print.grid

import po.misc.data.pretty_print.cells.KeyedCell
import po.misc.data.pretty_print.cells.StaticCell
import po.misc.data.pretty_print.PrettyGrid
import po.misc.data.pretty_print.PrettyValueGrid
import po.misc.data.pretty_print.grid.buildPrettyGrid
import po.misc.data.pretty_print.grid.buildRow
import po.misc.data.pretty_print.parts.Orientation
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.rows.buildPrettyRow
import po.misc.data.pretty_print.rows.buildRowContainer
import po.test.misc.data.pretty_print.setup.PrettyTestBase
import po.test.misc.data.pretty_print.setup.PrintableElement
import po.test.misc.data.pretty_print.setup.PrintableRecord
import po.test.misc.data.pretty_print.setup.PrintableRecordSubClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestTemplateContainer : PrettyTestBase() {

    private val record = createRecord()

    private val subClassGrid: PrettyGrid<PrintableRecordSubClass> = buildPrettyGrid<PrintableRecordSubClass>() {
        buildRow(Row.SubTemplateRow) {
            addAll(PrintableRecordSubClass::subName, PrintableRecordSubClass::subComponent)
        }
    }
    private val elementGrid: PrettyGrid<PrintableElement> = buildPrettyGrid {
        buildRow(Row.SubTemplateRow) {
            add(PrintableElement::elementName)
        }
    }
    private val subClassRow = buildPrettyRow<PrintableRecordSubClass>(Row.SubTemplateRow) {
        addAll(PrintableRecordSubClass::subName, PrintableRecordSubClass::subComponent)
    }
    private val elementsRow = record.elements.buildRowContainer {
        add(PrintableElement::elementName)
    }

    @Test
    fun `Template by prettyRow is created as expected`() {

        val grid = buildPrettyGrid<PrintableRecord>(Grid.Grid1) {
            useTemplate(subClassRow, PrintableRecord::subClass) {
                headedRow(headerText1, Row.Row1)
            }
        }
        val template = assertIs<PrettyValueGrid<PrintableRecord, PrintableRecordSubClass>>(grid.renderMap.renderables.first())
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
                headedRow(headerText1, Row.Row1)
            }
        }
        val template = assertIs<PrettyGrid<PrintableElement>>(grid.renderMap.renderables.firstOrNull())
        val firstRow = assertNotNull(template.rows.firstOrNull())
        val secondRow = assertNotNull(template.rows.getOrNull(1))
        assertIs<KeyedCell<PrintableElement>>(firstRow.cells.firstOrNull())
        assertEquals(1, firstRow.cells.size)
        assertIs<StaticCell>(secondRow.cells.firstOrNull())
        assertEquals(1, secondRow.cells.size)
        assertEquals(2, template.rows.size)
        assertEquals(1, elementsRow.cells.size)
    }

    @Test
    fun `Exactly one render block produced`() {
        val grid = buildPrettyGrid<PrintableRecord>(Grid.Grid1) {
            useTemplate(subClassGrid, PrintableRecord::subClass) {
                headedRow(headerText1)
            }
        }
        assertEquals(1, grid.renderMap.renderables.size)
    }

    @Test
    fun `Template is inserted as first if renderHere function not called`() {
        val grid = buildPrettyGrid<PrintableRecord>(Grid.Grid1) {
            useTemplate(subClassGrid, PrintableRecord::subClass) {
                buildRow {
                    add(headerText1)
                }
            }
        }

        assertEquals(1, grid.renderMap.renderables.size)
        val subGrid =   assertIs<PrettyValueGrid<PrintableRecord, PrintableRecordSubClass>>(grid.renderMap.renderables.first())
        assertEquals(2, subGrid.rows.size)
        val firstRow  = assertIs<PrettyRow<PrintableRecordSubClass>>(subGrid.rows[0])
        val secondRow  = assertIs<PrettyRow<PrintableRecordSubClass>>(subGrid.rows[1])
        assertEquals(PrintableRecordSubClass::class, firstRow.hostType.kClass)
        assertEquals(Row.SubTemplateRow, firstRow.id)
        assertEquals(PrintableRecordSubClass::class, secondRow.hostType.kClass)
        assertEquals(1, secondRow.cells.size)
        val staticCell  = assertIs<StaticCell>(secondRow.cells[0])
        assertEquals(headerText1, staticCell.text)
    }

    @Test
    fun `Template is inserted to a specific position`() {
        val grid = buildPrettyGrid<PrintableRecord>(Grid.Grid1) {
            useTemplate(subClassGrid, PrintableRecord::subClass) {
                headedRow(headerText1, Row.Row1)
                renderHere()
                headedRow(headerText2, Row.Row2)
            }
        }
        val template = assertIs<PrettyValueGrid<PrintableRecord, PrintableRecordSubClass>>(grid.renderMap.renderables.first())
        assertNotNull(template.rows.firstOrNull()) {
            assertEquals(Row.Row1, it.id)
        }
        assertNotNull(template.rows.getOrNull(1)) { row ->
            assertIs<PrettyRow<PrintableRecordSubClass>>(row)
            assertEquals(PrintableRecordSubClass::class, row.hostType.kClass)
            assertEquals(2, row.cells.size)
            assertEquals(Row.SubTemplateRow, row.id)
        }
        assertNotNull(template.rows.getOrNull(2)) {
            assertEquals(Row.Row2, it.id)
        }
    }

    @Test
    fun `Base template is inserted even if block is empty`() {
        val grid = buildPrettyGrid<PrintableRecord>(Grid.Grid1) {
            useTemplate(subClassGrid, PrintableRecord::subClass) {

            }
        }
        val template = assertIs<PrettyValueGrid<PrintableRecord, PrintableRecordSubClass>>(grid.renderMap.elements.first())
        assertNotNull(template.rows.firstOrNull()) {
            assertEquals(Row.SubTemplateRow, it.id)
        }
    }

    @Test
    fun `List type template is created`() {
        val grid = buildPrettyGrid<PrintableRecord>(Grid.Grid1) {
            useTemplate(elementGrid, PrintableRecord::elements) {
                headedRow(headerText1, Row.Row1)
                renderHere()
            }
        }
        val template = assertIs<PrettyValueGrid<PrintableRecord, PrintableRecordSubClass>>(grid.renderMap.elements.first())
        assertTrue(template.listLoader.hasProperty)
        assertNotNull(template.rows.getOrNull(1)) { row ->
            assertIs<PrettyRow<PrintableElement>>(row)
            assertEquals(PrintableElement::class, row.hostType.kClass)
        }
    }

    @Test
    fun `Template builder additional features work as expected`() {
        val grid = buildPrettyGrid<PrintableRecord> {
            useTemplate(elementGrid, PrintableRecord::elements) {
                useId(Grid.SubTemplateGrid)
                orientation = Orientation.Vertical
                exclude(Cell.Cell1, Cell.Cell2, Cell.Cell3, Cell.Cell4)
                headedRow(headerText1, Row.Row1)
            }
        }
        val template = assertIs<PrettyValueGrid<PrintableRecord, PrintableRecordSubClass>>(grid.renderMap.elements.first())
        assertEquals(Grid.SubTemplateGrid, template.id)
        assertEquals(Orientation.Vertical, template.options.orientation)
        assertEquals(4, template.options.excludeFromRenderList.size)
        assertEquals(Cell.Cell1, template.options.excludeFromRenderList.firstOrNull())
    }
}