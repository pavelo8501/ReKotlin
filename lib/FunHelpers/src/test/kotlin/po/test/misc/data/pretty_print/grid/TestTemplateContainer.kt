package po.test.misc.data.pretty_print.grid

import po.misc.data.pretty_print.cells.StaticCell
import po.misc.data.pretty_print.PrettyGrid
import po.misc.data.pretty_print.PrettyValueGrid
import po.misc.data.pretty_print.buildPrettyGrid
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.buildPrettyRow
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
    val headerText1: String = "header_text_1"
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

    @Test
    fun `Exactly one render block produced`() {
        val grid = buildPrettyGrid<PrintableRecord>(Grid.Grid1) {
            useGrid(subClassGrid, PrintableRecord::subClass) {
                headedRow(headerText1)
            }
        }
        assertEquals(1, grid.renderPlan.renderNodes.size)
    }

    @Test
    fun `Template is inserted as first if renderHere function not called`() {
        val grid = buildPrettyGrid<PrintableRecord>(Grid.Grid1) {
            useGrid(subClassGrid, PrintableRecord::subClass) {
//                buildRow {
//                    add(headerText1)
//                }
            }
        }

        assertEquals(1, grid.renderPlan.renderNodes.size)
        val subGrid =   assertIs<PrettyValueGrid<PrintableRecord, PrintableRecordSubClass>>(grid.renderPlan.renderNodes.first())
        assertEquals(2, subGrid.rows.size)
        val firstRow  = assertIs<PrettyRow<PrintableRecordSubClass>>(subGrid.rows[0])
        val secondRow  = assertIs<PrettyRow<PrintableRecordSubClass>>(subGrid.rows[1])
        assertEquals(PrintableRecordSubClass::class, firstRow.typeToken.kClass)
        assertEquals(Row.SubTemplateRow, firstRow.templateID)
        assertEquals(PrintableRecordSubClass::class, secondRow.typeToken.kClass)
        assertEquals(1, secondRow.cells.size)
        val staticCell  = assertIs<StaticCell>(secondRow.cells[0])
        assertEquals(headerText1, staticCell.text)
    }

    @Test
    fun `Template is inserted to a specific position`() {
        val grid = buildPrettyGrid<PrintableRecord>(Grid.Grid1) {
            useGrid(subClassGrid, PrintableRecord::subClass) {
                headedRow(headerText1, Row.Row1)
                renderSourceHere()
                headedRow(headerText2, Row.Row2)
            }
        }
        val template = assertIs<PrettyValueGrid<PrintableRecord, PrintableRecordSubClass>>(grid.renderPlan.renderNodes.first())
        assertNotNull(template.rows.firstOrNull()) {
            assertEquals(Row.Row1, it.templateID)
        }
        assertNotNull(template.rows.getOrNull(1)) { row ->
            assertIs<PrettyRow<PrintableRecordSubClass>>(row)
            assertEquals(PrintableRecordSubClass::class, row.typeToken.kClass)
            assertEquals(2, row.cells.size)
            assertEquals(Row.SubTemplateRow, row.templateID)
        }
        assertNotNull(template.rows.getOrNull(2)) {
            assertEquals(Row.Row2, it.templateID)
        }
    }

    @Test
    fun `Base template is inserted even if block is empty`() {
        val grid = buildPrettyGrid<PrintableRecord>(Grid.Grid1) {
            useGrid(subClassGrid, PrintableRecord::subClass) {

            }
        }
        val template = assertIs<PrettyValueGrid<PrintableRecord, PrintableRecordSubClass>>(grid.renderPlan.renderNodes.first())
        assertNotNull(template.rows.firstOrNull()) {
            assertEquals(Row.SubTemplateRow, it.templateID)
        }
    }

    @Test
    fun `List type template is created`() {
        val grid = buildPrettyGrid<PrintableRecord>(Grid.Grid1) {
            useGrid(elementGrid, PrintableRecord::elements) {
                headedRow(headerText1, Row.Row1)
                renderSourceHere()
            }
        }
        val template = assertIs<PrettyValueGrid<PrintableRecord, PrintableRecordSubClass>>(grid.renderPlan.renderNodes.first())
        assertTrue(template.dataLoader.hasProperty)
        assertNotNull(template.rows.getOrNull(1)) { row ->
            assertIs<PrettyRow<PrintableElement>>(row)
            assertEquals(PrintableElement::class, row.typeToken.kClass)
        }
    }

    @Test
    fun `Template builder additional features work as expected`() {
        val grid = buildPrettyGrid<PrintableRecord> {
            useGrid(elementGrid, PrintableRecord::elements) {
                orientation = Orientation.Vertical
                headedRow(headerText1, Row.Row1)
            }
        }
//        val template = assertIs<PrettyValueGrid<PrintableRecord, PrintableRecordSubClass>>(grid.renderPlan.renderables.first())
//
//        assertEquals(Grid.SubTemplateGrid, template.id)
//        assertEquals(Orientation.Vertical, template.options.orientation)
//        assertEquals(4, template.options.excludeFromRenderList.size)
//        assertEquals(Cell.Cell1, template.options.excludeFromRenderList.firstOrNull())
    }
}