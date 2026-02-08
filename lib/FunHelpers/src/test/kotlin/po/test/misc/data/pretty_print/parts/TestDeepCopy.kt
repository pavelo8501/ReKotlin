package po.test.misc.data.pretty_print.parts

import po.misc.callbacks.callable.asProvider
import po.misc.collections.asList
import po.misc.data.logging.Verbosity
import po.misc.data.output.output
import po.misc.data.pretty_print.PrettyGrid
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.PrettyValueGrid
import po.misc.data.pretty_print.buildGrid
import po.misc.data.pretty_print.cells.KeyedCell
import po.misc.data.pretty_print.cells.PrettyCell
import po.misc.data.pretty_print.cells.StaticCell
import po.misc.data.pretty_print.dsl.DSLEngine
import po.misc.data.pretty_print.parts.common.Grid
import po.misc.data.pretty_print.parts.common.Row
import po.misc.data.pretty_print.parts.loader.DataLoader
import po.misc.data.pretty_print.parts.loader.toElementProvider
import po.misc.data.pretty_print.parts.options.DefaultGridID
import po.misc.data.pretty_print.parts.options.DefaultRowID
import po.misc.data.strings.contains
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
        row.templateData.templateID.output(verbosity)
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
        row.templateData.output(verbosity)
        assertTrue { row.templateData.templateID.typeName.contains(Row.Row1) }
        assertEquals(row.templateData.templateID, row.templateID)
    }

    @Test
    fun `Grid templateID creation logic`() {
        val genericIDGrid = PrettyGrid(TypeToken<TestDeepCopy>())
        genericIDGrid.templateData.templateID.output(verbosity)
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
        genericIDGrid.templateData.templateID.output(verbosity)
        assertTrue { genericIDGrid.templateData.templateID.typeName.contains("TestDeepCopy") }
        assertEquals(genericIDGrid.templateData.templateID, genericIDGrid.templateID)
        assertIs<DefaultGridID>(genericIDGrid.templateID)

        val grid = buildGrid<TestDeepCopy>(Grid.Grid1) {

        }
        assertIs<Grid>(grid.templateData.templateID)
        grid.templateData.output(verbosity)
        assertTrue { grid.templateData.templateID.typeName.contains(Grid.Grid1) }
        assertEquals(grid.templateData.templateID, grid.templateID)
    }

}