package po.test.misc.data.pretty_print.parts

import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import po.misc.collections.asList
import po.misc.collections.repeatBuild
import po.misc.data.contains
import po.misc.data.logging.Verbosity
import po.misc.data.output.output
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.dsl.InTestDSL
import po.misc.data.pretty_print.parts.grid.RowNode
import po.misc.data.pretty_print.parts.grid.TransitionNode
import po.misc.data.pretty_print.parts.grid.ValueGridNode
import po.misc.data.pretty_print.parts.loader.DataProvider
import po.misc.reflection.displayName
import po.misc.types.token.TypeLitera
import po.misc.types.token.TypeToken
import po.misc.types.token.acceptsReceiverOf
import po.misc.types.token.resolvesValueOf
import po.test.misc.data.pretty_print.setup.PrettyTestBase
import po.test.misc.data.pretty_print.setup.PrintableRecord
import po.test.misc.data.pretty_print.setup.PrintableRecordSubClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestRenderPlan : PrettyTestBase() {

    private class ForeignSubClass(var name: String = "Foreign Sub Class")
    private class ForeignClass(
        var name: String = "Foreign",
        var subClass: ForeignSubClass = ForeignSubClass()
    )

    private val inTestDsl = InTestDSL()

    private val record = createRecord()
    private val text: String = "Text property"
    private val value: Int = 300
    private val textList: List<String> = listOf("Text 1", "Text 2", "Text 3")

    @Test
    fun `Correct node types created for render plans of same receiver resolvance chain`() {
        val grid = inTestDsl.createGrid<TestRenderPlan>(Grid.Grid1)
        val renderPlan = grid.renderPlan
        val row = PrettyRow.createEmpty<TestRenderPlan>()
        val row2 = PrettyRow.createEmpty<TestRenderPlan>()
        renderPlan.add(row)
        renderPlan.add(row2)
        val nodes = renderPlan.getNodesOrdered()
        assertEquals(2, renderPlan.size)
        assertEquals(2, nodes.size)
        assertIs<RowNode<TestRenderPlan>>(nodes[0])
        assertIs<RowNode<TestRenderPlan>>(nodes[1])

        val valueGrid = inTestDsl.createValueGrid(TestRenderPlan::record)
        val valueNode = renderPlan.add(valueGrid)
        assertEquals(3, renderPlan.size)
        assertIs<ValueGridNode<TestRenderPlan, PrintableRecord>>(valueNode)

        assertTrue { valueGrid.acceptsReceiverOf<TestRenderPlan>() }
        assertTrue { valueGrid.resolvesValueOf<PrintableRecord>() }
        assertSame(valueGrid, valueNode.element)
        assertEquals(true, valueNode.hasRenderPlan)
        assertSame(valueGrid, valueNode.renderPlan.host)
    }

    @Test
    fun `Nodes created correctly for same receiver chain`() {
        val grid = inTestDsl.createGrid<TestRenderPlan>(Grid.Grid1)
        val transitionGrid = inTestDsl.createTransitionGrid<ForeignClass>(Grid.ForeignGrid)
        val renderPlan = grid.renderPlan
        val foreignNode = renderPlan.add(transitionGrid)

        assertEquals(1, renderPlan.size)
        assertEquals(0, renderPlan.renderBacking.size)
        assertIs<TransitionNode<ForeignClass, ForeignClass>>(foreignNode)
        assertSame(transitionGrid, foreignNode.element)
        assertEquals(true, foreignNode.hasRenderPlan)
        assertSame(transitionGrid, foreignNode.renderPlan.host)
        assertTrue { foreignNode.acceptsReceiverOf<ForeignClass>() }
        assertDoesNotThrow {
            renderPlan.renderList(this.asList())
        }
    }

    @Test
    fun `Transition grid gracefully handled by render plan`() {

        val grid = inTestDsl.createGrid<TestRenderPlan>(Grid.Grid1)
        val renderPlan = grid.renderPlan
        val source = inTestDsl.createGrid<ForeignSubClass>(Grid.Grid2)
        val row = inTestDsl.buildRow<ForeignSubClass>(Row.Row1) { add(ForeignSubClass::name) }
        source.addRow(row)

        val provider = DataProvider(ForeignSubClass::class){ ForeignSubClass() }
        val transitionGrid = inTestDsl.createTransitionGrid(source, provider )
        renderPlan.add(transitionGrid)
        val testRenderPlanRow = inTestDsl.buildRow<TestRenderPlan>(Row.Row1) { add(TestRenderPlan::text) }
        renderPlan.add(testRenderPlanRow)

        val transitionNode = assertIs<TransitionNode<*, *>>(renderPlan.foreignNodes.first())
        assertEquals(2, renderPlan.size)
        assertEquals(1, renderPlan.foreignNodes.size)
        assertSame(transitionGrid, transitionNode.element)
        assertEquals(true, grid.enabled, "Should be enabled by default")
        assertEquals(true, transitionNode.element.enabled)
        assertEquals(true, transitionGrid.dataLoader.canResolve)
        assertEquals(true, transitionGrid.dataLoader.hasProvider)
        assertEquals(1, transitionNode.renderPlan.size)
        val render = grid.render(this)
        val lines = render.lines()
        assertEquals(2, lines.size)
        assertTrue { lines[0].contains("Foreign Sub Class") }
        assertTrue { lines[1].contains(text) }
    }

    @Test
    fun `Multiple rows render one receiver class passed as list`() {
        val grid = inTestDsl.createGrid<TestRenderPlan>(Grid.Grid1)
        val renderPlan = grid.renderPlan
        val row = inTestDsl.buildRow<TestRenderPlan>(Row.Row1) { add(TestRenderPlan::text) }
        val row2 = inTestDsl.buildRow<TestRenderPlan>(Row.Row2) { add(TestRenderPlan::value) }
        renderPlan.add(row)
        renderPlan.add(row2)
        val render = grid.render(this)
        val lines = render.lines()
        assertEquals(2, lines.size)
        assertTrue { lines[0].contains(text) }
        assertTrue { lines[1].contains(value.toString()) }
    }

    @Test
    fun `Single row handle receiver of type list rows render one receiver class passed as list`() {
        val grid = inTestDsl.createGrid<PrintableRecordSubClass>(Grid.Grid1)
        val renderPlan = grid.renderPlan
        val row = inTestDsl.buildRow<PrintableRecordSubClass>(Row.Row1) { add(PrintableRecordSubClass::subName) }
        renderPlan.add(row)
        val input = 4.repeatBuild { PrintableRecordSubClass(subName = "Name_$it") }
        val render = grid.render(input)
        val lines = render.lines()
        assertEquals(4, lines.size)
        assertTrue {
            lines[0].contains(input[0].subName) &&
                    lines[0].contains(PrintableRecordSubClass::subName.displayName)
        }
    }
}