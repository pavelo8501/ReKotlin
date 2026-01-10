package po.test.misc.data.pretty_print.parts

import po.misc.callbacks.callable.asProvider
import po.misc.collections.asList
import po.misc.data.contains
import po.misc.data.output.output
import po.misc.data.pretty_print.PrettyGrid
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.PrettyValueGrid
import po.misc.data.pretty_print.buildGrid
import po.misc.data.pretty_print.cells.KeyedCell
import po.misc.data.pretty_print.cells.PrettyCell
import po.misc.data.pretty_print.cells.StaticCell
import po.misc.data.pretty_print.dsl.DSLEngine
import po.misc.data.pretty_print.parts.loader.DataLoader
import po.misc.data.pretty_print.parts.loader.toElementProvider
import po.misc.data.pretty_print.parts.options.DefaultGridID
import po.misc.data.pretty_print.parts.options.DefaultRowID
import po.misc.functions.CallableKey
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf
import po.test.misc.data.pretty_print.setup.PrettyTestBase
import po.test.misc.data.pretty_print.setup.PrintableRecord
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

class TestDeepCopy : PrettyTestBase() {

    private val template1 = buildRow(Row.Row1) {
        add(PrintableRecord::name)
        add("Static")
    }
    private val someString: String = "Some string"
    private val record = createRecord()
    private val testDeepToken = tokenOf<TestDeepCopy>()

    @Test
    fun `Cell copy extension work as expected`() {
        val static = StaticCell("Some text")
        val staticCopy = static.copy()
        assertNotSame(static, staticCopy)
        assertEquals(static.text, staticCopy.text)

        val pretty = PrettyCell()
        val prettyCopy = pretty.copy()
        assertNotSame(pretty, prettyCopy)

        val keyed0 = KeyedCell<TestDeepCopy>(::someString)
        val keyed0Copy = keyed0.copy()
        assertNotSame(keyed0, keyed0Copy)
        assertEquals(keyed0.sourceType, keyed0Copy.sourceType)
        assertEquals(keyed0.sourceType, TypeToken<TestDeepCopy>())
    }

    @Test
    fun `Value loaders copy test`() {
        val token1 = tokenOf<TestDeepCopy>()
        val token2 = tokenOf<PrintableRecord>()

        val loader = DataLoader("Test", token1, token2)
        loader.apply(TestDeepCopy::record.toElementProvider())
        val loader2Copy = loader.copy()
        assertNotSame(loader, loader2Copy)
        assertEquals(loader, loader2Copy)
        assertNotNull(loader2Copy[CallableKey.Property])
    }

    @Test
    fun `Row copy as expected`() {
        val rowCopy: PrettyRow<PrintableRecord> = template1.copy()
        assertNotSame(template1, rowCopy)
        assertEquals(template1, rowCopy)
    }

    @Test
    fun `Row added to builder is not mutated internally `() {
//        val rowContainer = RowBuilder(template1)
//        assertSame(template1, rowContainer.prettyRow)
//        assertEquals(template1, rowContainer.prettyRow)
    }

    @Test
    fun `Row templateID creation logic`() {
        val row = PrettyRow<TestDeepCopy>()
        row.templateData.templateID.output(enableOutput)
        assertTrue { row.templateData.templateID.typeName.contains("TestDeepCopy") }
        assertEquals(row.templateData.templateID, row.templateID)
    }

    @Test
    fun `Row templateID creation logic in builders`() {
        val dsl = DSLEngine()
        val genericIDRow = dsl.buildRow<TestDeepCopy> {

        }
        assertIs<DefaultRowID>(genericIDRow.templateData.templateID)

        val row = dsl.buildRow<TestDeepCopy>(Row.Row1) {

        }
        assertIs<Row>(row.templateData.templateID)
        row.templateData.output(enableOutput)
        assertTrue { row.templateData.templateID.typeName.contains(Row.Row1) }
        assertEquals(row.templateData.templateID, row.templateID)
    }

    @Test
    fun `Grid templateID creation logic`() {
        val genericIDGrid = PrettyGrid(TypeToken<TestDeepCopy>())
        genericIDGrid.templateData.templateID.output(enableOutput)
        assertTrue { genericIDGrid.templateData.templateID.typeName.contains("TestDeepCopy") }
        assertEquals(genericIDGrid.templateData.templateID, genericIDGrid.templateID)
        assertIs<DefaultGridID>(genericIDGrid.templateID)

        val grid = PrettyGrid(TypeToken<TestDeepCopy>(), gridID = Grid.Grid1)
        assertTrue { grid.templateData.templateID.typeName.contains(Grid.Grid1) }
        assertIs<Grid>(grid.templateID)
    }

    @Test
    fun `Grid templateID creation logic in builders`() {

        val genericIDGrid = buildGrid<TestDeepCopy> {

        }
        genericIDGrid.templateData.templateID.output(enableOutput)
        assertTrue { genericIDGrid.templateData.templateID.typeName.contains("TestDeepCopy") }
        assertEquals(genericIDGrid.templateData.templateID, genericIDGrid.templateID)
        assertIs<DefaultGridID>(genericIDGrid.templateID)

        val grid = buildGrid<TestDeepCopy>(Grid.Grid1) {

        }
        assertIs<Grid>(grid.templateData.templateID)
        grid.templateData.output(enableOutput)
        assertTrue { grid.templateData.templateID.typeName.contains(Grid.Grid1) }
        assertEquals(grid.templateData.templateID, grid.templateID)
    }

    @Test
    fun `Render plan creates exact copy of its internals preserving order`() {
        val grid = PrettyGrid<TestDeepCopy>(Grid.Grid1)
        val valueGridSource = PrettyGrid<PrintableRecord>()
        //val provider = DataProvider(ProviderCallable, testDeepToken) { PrintableRecord() }

        val provider = asProvider{ PrintableRecord() }

        val valueGrid = PrettyValueGrid.createFromGrid(provider, valueGridSource)

        val row1 = PrettyRow<PrintableRecord>(Row.Row1)
        val row2 = PrettyRow<PrintableRecord>()

        val cell1OfRow1 = StaticCell("cell1OfRow1")
        row1.initCells(cell1OfRow1.asList())
        valueGrid.addRow(row1)

        val cell1OfRow2 = StaticCell("cell1OfRow2")
        row2.initCells(cell1OfRow2.asList())
        valueGrid.addRow(row2)
        grid.renderPlan.add(valueGrid)

        assertNotNull(grid.renderPlan[PrettyValueGrid].firstOrNull()) { valGrid ->
            val rows = valGrid.renderPlan[PrettyRow]
            assertNotNull(rows.getOrNull(0)) { row1 ->
                assertNotNull(row1.cells.firstOrNull())
                assertEquals(Row.Row1, row1.templateID)
            }
            assertNotNull(rows.getOrNull(1)) { row2 ->
                assertNotNull(row2.cells.firstOrNull())
                assertNotEquals(Row.Row1, row2.templateID)
            }
        }
        val renderPlanCopy = grid.renderPlan.copy()
        assertSame(grid, renderPlanCopy.host)
        assertNotNull(renderPlanCopy[PrettyValueGrid].firstOrNull()){copiedValueGrid ->
            assertNotSame(valueGrid, copiedValueGrid)
            assertEquals(valueGrid.templateID, copiedValueGrid.templateID, "TemplateIds different")
            assertEquals(valueGrid.size, copiedValueGrid.size, "Size mismatch")
            assertEquals(valueGrid.dataLoader, copiedValueGrid.dataLoader, "Data loaders not equal")
            assertEquals(valueGrid.prettyRows, copiedValueGrid.prettyRows, "Pretty rows list not equal")
            assertEquals(valueGrid, copiedValueGrid)
        }
    }
}