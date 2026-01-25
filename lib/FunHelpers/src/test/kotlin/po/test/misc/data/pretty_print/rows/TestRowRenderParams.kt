package po.test.misc.data.pretty_print.rows

import po.misc.data.output.output
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.Templated
import po.misc.data.pretty_print.cells.PrettyCell
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.parts.options.RowOptions
import po.misc.data.pretty_print.parts.rows.ValueRenderNode
import po.misc.data.pretty_print.parts.rows.BoundRenderNode
import po.misc.data.pretty_print.parts.rows.StaticRenderNode
import po.misc.data.pretty_print.parts.rows.Layout
import po.misc.reflection.displayName
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class TestRowRenderParams : Templated<TestRowRenderParams> {

    override val receiverType: TypeToken<TestRowRenderParams> = tokenOf()

    private val text1 = "Text 1"
    private val text2 = "Text 2 bit longer"
    private val text3 = "Text 3 longer than text 2"

    private val staticCell1 = text1.toCell()
    private val prettyCell = PrettyCell()
    private val keyedCell = TestRowRenderParams::text3.toCell()
    private val enableOutput = true

    @Test
    fun `Row rendering Layout Compact mixed types`(){
        val options = RowOptions(Orientation.Horizontal, Layout.Compact)
        val row = PrettyRow(options, staticCell1, prettyCell)
        val render =  row.renderAny(text2)
        render.output(enableOutput)
        val lines = render.lines()
        assertEquals(2, row.planner.nodes.size)
        val staticNode =  assertIs<StaticRenderNode>(row.planner[0])
        val valueNode =  assertIs<ValueRenderNode>(row.planner[1])
        assertEquals(text1.length, staticNode.contentWidth, "Wrong size for StaticRenderNode")
        assertEquals(text2.length, valueNode.contentWidth, "Wrong size for ValueRenderNode")
        assertEquals(1, lines.size)
    }

    @Test
    fun `Row rendering Layout Stretch`(){
        val options = RowOptions(Orientation.Horizontal, Layout.Stretch)
        val row = PrettyRow(options, staticCell1, prettyCell)
        val render =  row.renderAny(text2)
        render.output(enableOutput)
        val lines = render.lines()
        val firsParam =  assertIs<StaticRenderNode>(row.planner.nodes.first())
        val secondParam =  assertIs<ValueRenderNode>(row.planner.nodes.last())
        val cellWidth = (options.viewport?.size?:0) / 2
        assertEquals(1, lines.size)
        assertEquals(cellWidth, firsParam.contentWidth, "Wrong size for RenderParamStatic")
        assertEquals(cellWidth, secondParam.contentWidth, "Wrong size for RenderParamAny")
        assertTrue { lines[0].contains(text1) && lines[0].contains(text2) }
    }

    @Test
    fun `Row rendering Layout Compact mixed types including source aware`(){
        val options = RowOptions(Orientation.Horizontal, Layout.Compact)
        val row = PrettyRow<TestRowRenderParams>(options, listOf(prettyCell, keyedCell))
        val render =  row.renderAny(text2, this)
        render.output(enableOutput)
        val lines = render.lines()
        val valueNode =  assertIs<ValueRenderNode>(row.planner[0])
        val boundNode =  assertIs<BoundRenderNode<TestRowRenderParams>>(row.planner[1])
        val propertyNameLength = ::text3.displayName.length
        val keySeparatorLength = prettyCell.cellOptions.keySeparator.toString().length
        val keyedSize = text3.length + propertyNameLength + keySeparatorLength
        assertEquals(1, lines.size)
        assertEquals(text2.length, valueNode.contentWidth, "Wrong size for ValueRenderNode")
        assertEquals(keyedSize, boundNode.contentWidth, "Wrong size for BoundRenderNode")
        assertTrue { lines[0].contains(text2) && lines[0].contains(text3) }
    }

    @Test
    fun `Row rendering Layout Compact  keyed only`(){
        val options = RowOptions(Orientation.Horizontal, Layout.Compact)
        val keyed1 =  TestRowRenderParams::text1.toCell()
        val keyed2 = TestRowRenderParams::text2.toCell()
        val keyed3 =  TestRowRenderParams::text3.toCell()
        val keySeparatorLength = prettyCell.cellOptions.keySeparator.toString().length
        val propertyNameLength = ::text1.displayName.length
        val row = PrettyRow(options, keyed1, keyed2, keyed3)

        row.planner.createRenderNodes(row.cells)
        val keyed1Node =  assertIs<BoundRenderNode<TestRowRenderParams>>(row.planner[0])
        val secundParam =  assertIs<BoundRenderNode<TestRowRenderParams>>(row.planner[1])
        val thirdParam =  assertIs<BoundRenderNode<TestRowRenderParams>>(row.planner[2])
        val render =  row.render(this)
        render.output(enableOutput)
        val lines = render.lines()
        assertEquals(1, lines.size)
        assertTrue { lines[0].contains(text1) && lines[0].contains(text2)  && lines[0].contains(text3) }
        assertEquals(propertyNameLength + keySeparatorLength + text1.length, keyed1Node.contentWidth, "Wrong size for firsParam")
        assertEquals(propertyNameLength + keySeparatorLength + text2.length, secundParam.contentWidth, "Wrong size for secundParam")
        assertEquals(propertyNameLength + keySeparatorLength + text3.length, thirdParam.contentWidth, "Wrong size for thirdParam")
    }
}