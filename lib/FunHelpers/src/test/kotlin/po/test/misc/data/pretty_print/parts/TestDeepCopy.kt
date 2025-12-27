package po.test.misc.data.pretty_print.parts

import po.misc.collections.lambda_map.CallableDescriptor
import po.misc.data.contains
import po.misc.data.output.output
import po.misc.data.pretty_print.PrettyGrid
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.cells.KeyedCell
import po.misc.data.pretty_print.cells.PrettyCell
import po.misc.data.pretty_print.cells.StaticCell
import po.misc.data.pretty_print.dsl.DSLEngine
import po.misc.data.pretty_print.parts.loader.DataLoader
import po.misc.data.pretty_print.parts.template.DefaultID
import po.misc.data.pretty_print.rows.RowBuilder
import po.misc.data.pretty_print.toProvider
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf
import po.test.misc.data.pretty_print.setup.PrettyTestBase
import po.test.misc.data.pretty_print.setup.PrintableRecord
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertTrue

class TestDeepCopy : PrettyTestBase() {

    private val template1 = buildRow(Row.Row1) {
        add(PrintableRecord::name)
        add("Static")
    }
    private val someString: String = "Some string"
    private val record = createRecord()

    @Test
    fun `Cell copy extension work as expected`(){
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
        assertEquals(keyed0.receiverType, keyed0Copy.receiverType)
        assertEquals(keyed0.receiverType, TypeToken<TestDeepCopy>())
    }

    @Test
    fun `Value loaders copy test`(){
        val token1 = tokenOf<TestDeepCopy>()
        val token2 = tokenOf<PrintableRecord>()

        val loader = DataLoader("Test", token1, token2)
        loader.applyCallables(TestDeepCopy::record.toProvider())
        val loader2Copy = loader.copy()
        assertNotSame(loader, loader2Copy)
        assertEquals(loader, loader2Copy)
        assertNotNull(loader2Copy[CallableDescriptor.CallableKey.ReadOnlyProperty])
    }

    @Test
    fun `Row copy as expected`(){
        val rowCopy : PrettyRow<PrintableRecord> = template1.copy()
        assertNotSame(template1, rowCopy)
        assertEquals(template1, rowCopy)
    }

    @Test
    fun `Row copy by container work as expected`(){
        val rowContainer = RowBuilder(template1)
        assertNotSame(template1, rowContainer.prettyRow)
        assertEquals(template1, rowContainer.prettyRow)
    }
    @Test
    fun `Row templateID creation logic`(){
        val row = PrettyRow.createEmpty<TestDeepCopy>()
        row.templateData.templateID.output()
        assertTrue { row.templateData.templateID.typeName.contains("TestDeepCopy") }
        assertEquals(row.templateData.templateID, row.id)
    }

    @Test
    fun `Row templateID creation logic in builders`(){
        val dsl = DSLEngine()
        val genericIDRow = dsl.buildRow<TestDeepCopy>{

        }
        assertIs<DefaultID>(genericIDRow.templateData.templateID)

        val row = dsl.buildRow<TestDeepCopy>(Row.Row1) {

        }
        assertIs<Row>(row.templateData.templateID)
        row.templateData.output()
        assertTrue { row.templateData.templateID.typeName.contains(Row.Row1) }
        assertEquals(row.templateData.templateID, row.id)
    }

    @Test
    fun `Grid templateID creation logic`(){
        val  genericIDGrid =  PrettyGrid(TypeToken<TestDeepCopy>())
        genericIDGrid.templateData.templateID.output()
        assertTrue {genericIDGrid.templateData.templateID.typeName.contains("TestDeepCopy") }
        assertEquals(genericIDGrid.templateData.templateID, genericIDGrid.id)
        assertIs<DefaultID>(genericIDGrid.id)

        val grid = PrettyGrid(TypeToken<TestDeepCopy>(), gridID = Grid.Grid1)
        assertTrue {grid.templateData.templateID.typeName.contains(Grid.Grid1) }
        assertIs<Grid>(grid.id)
    }

    @Test
    fun `Grid templateID creation logic in builders`(){
        val dsl = DSLEngine()
        val genericIDGrid = dsl.buildGrid<TestDeepCopy> {

        }
        genericIDGrid.templateData.templateID.output()
        assertTrue {genericIDGrid.templateData.templateID.typeName.contains("TestDeepCopy") }
        assertEquals(genericIDGrid.templateData.templateID, genericIDGrid.id)
        assertIs<DefaultID>(genericIDGrid.id)

        val grid = dsl.buildGrid<TestDeepCopy>(Grid.Grid1) {

        }
        assertIs<Grid>(grid.templateData.templateID)
        grid.templateData.output()
        assertTrue { grid.templateData.templateID.typeName.contains(Grid.Grid1) }
        assertEquals(grid.templateData.templateID, grid.id)
    }

}