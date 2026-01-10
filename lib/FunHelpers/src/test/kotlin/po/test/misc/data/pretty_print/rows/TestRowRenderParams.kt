package po.test.misc.data.pretty_print.rows

import po.misc.data.helpers.lengthNoAnsi
import po.misc.data.output.output
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.Templated
import po.misc.data.pretty_print.cells.PrettyCell
import po.misc.data.pretty_print.parts.options.ViewPortSize
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.parts.options.RowOptions
import po.misc.data.pretty_print.parts.rendering.ValueRenderNode
import po.misc.data.pretty_print.parts.rendering.BoundRenderNode
import po.misc.data.pretty_print.parts.rendering.StaticRenderNode
import po.misc.data.pretty_print.parts.rows.RowLayout
import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars
import po.misc.reflection.displayName
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class TestRowRenderParams : Templated<TestRowRenderParams> {

    override val type: TypeToken<TestRowRenderParams> = tokenOf()

    private val text1 = "Text 1"
    private val text2 = "Text 2 bit longer"
    private val text3 = "Text 3 longer than text 2"

    private val staticCell1 = text1.toCell()
    private val prettyCell = PrettyCell()
    private val keyedCell = TestRowRenderParams::text3.toCell()
    private val enableOutput = true

    @Test
    fun `Row rendering RowLayout Compact mixed types`(){
        val options = RowOptions(Orientation.Horizontal, RowLayout.Compact)
        val row = PrettyRow(options, staticCell1, prettyCell)
        val render =  row.renderAny(text2)
        render.output(enableOutput)
        val lines = render.lines()
        assertEquals(2, row.planner.size)
        val staticNode =  assertIs<StaticRenderNode>(row.planner[0])
        val valueNode =  assertIs<ValueRenderNode>(row.planner[1])
        assertEquals(text1.length, staticNode.width, "Wrong size for StaticRenderNode")
        assertEquals(text2.length, valueNode.width, "Wrong size for ValueRenderNode")
        assertEquals(1, lines.size)
    }

    @Test
    fun `Row rendering RowLayout Stretch`(){
        val options = RowOptions(Orientation.Horizontal, RowLayout.Stretch)
        val row = PrettyRow(options, staticCell1, prettyCell)
        val render =  row.renderAny(text2)
        render.output(enableOutput)
        val lines = render.lines()
        val firsParam =  assertIs<StaticRenderNode>(row.planner.nodes.first())
        val secondParam =  assertIs<ValueRenderNode>(row.planner.nodes.last())
        val cellWidth = options.viewport.size / 2
        assertEquals(1, lines.size)
        assertEquals(cellWidth, firsParam.width, "Wrong size for RenderParamStatic")
        assertEquals(cellWidth, secondParam.width, "Wrong size for RenderParamAny")
        assertTrue { lines[0].contains(text1) && lines[0].contains(text2) }
    }

    @Test
    fun `Row rendering RowLayout Compact mixed types including source aware`(){
        val options = RowOptions(Orientation.Horizontal, RowLayout.Compact)
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
        assertEquals(text2.length, valueNode.width, "Wrong size for ValueRenderNode")
        assertEquals(keyedSize, boundNode.width, "Wrong size for BoundRenderNode")
        assertTrue { lines[0].contains(text2) && lines[0].contains(text3) }
    }

    @Test
    fun `Row rendering RowLayout Compact  keyed only`(){
        val options = RowOptions(Orientation.Horizontal, RowLayout.Compact)
        val keyed1 =  TestRowRenderParams::text1.toCell()
        val keyed2 = TestRowRenderParams::text2.toCell()
        val keyed3 =  TestRowRenderParams::text3.toCell()
        val keySeparatorLength = prettyCell.cellOptions.keySeparator.toString().length
        val propertyNameLength = ::text1.displayName.length
        val row = PrettyRow(options, keyed1, keyed2, keyed3)
        row.planner.createRenderNodes(row.cells, row.currentRenderOpt)
        val keyed1Node =  assertIs<BoundRenderNode<TestRowRenderParams>>(row.planner[0])
        val secundParam =  assertIs<BoundRenderNode<TestRowRenderParams>>(row.planner[1])
        val thirdParam =  assertIs<BoundRenderNode<TestRowRenderParams>>(row.planner[2])
        val render =  row.render(this)
        render.output(enableOutput)
        val lines = render.lines()
        assertEquals(1, lines.size)
        assertTrue { lines[0].contains(text1) && lines[0].contains(text2)  && lines[0].contains(text3) }
        assertEquals(propertyNameLength + keySeparatorLength + text1.length, keyed1Node.width, "Wrong size for firsParam")
        assertEquals(propertyNameLength + keySeparatorLength + text2.length, secundParam.width, "Wrong size for secundParam")
        assertEquals(propertyNameLength + keySeparatorLength + text3.length, thirdParam.width, "Wrong size for thirdParam")
    }

    @Test
    fun `Row indention for RowLayout Centered`(){
        val options = RowOptions(Orientation.Horizontal, RowLayout.Centered, ViewPortSize.Console120)
        val staticCell1 = text1.toCell()
        val staticCell2 = text2.toCell()
        val row = PrettyRow(options, staticCell1, staticCell2)
        val render = row.render()
        val separatorTotalLength = options.cellSeparator.size
        val projectedSize = row.planner.leftMargin + text1.length + text2.length + separatorTotalLength
        row.planner.output(enableOutput)
        render.output(enableOutput)
        assertEquals(projectedSize, render.length, "Actual string length  does not match projected size")
    }

    @Test
    fun `Row indention for RowLayout Centered with bottom border`(){
        val bottomBorderChar = '-'
         val options =  buildRowOption {
             orientation = Orientation.Horizontal
             layout = RowLayout.Centered
             viewport = ViewPortSize.Console120
             borders(borderColour = Colour.Green,  bottomBorder = bottomBorderChar)
         }
        val staticCell1 = text1.toCell()
        val staticCell2 = text2.toCell()
        val row = PrettyRow(options, staticCell1, staticCell2)
        val render = row.render()
        render.output(enableOutput)
        val lines = render.lines()
        val separatorTotalLength = options.cellSeparator.size
        val projectedSize = row.planner.leftMargin + text1.length + text2.length + separatorTotalLength
        assertEquals(2, lines.size)
        assertEquals(projectedSize, lines[0].length)
        assertEquals(projectedSize, lines[1].lengthNoAnsi)
        assertTrue {
            lines[1].contains(bottomBorderChar)
        }
    }

    @Test
    fun `Row indention for RowLayout Centered with top and bottom borders`(){
        val borderChar = '-'
        val options =  buildRowOption {
            orientation = Orientation.Horizontal
            layout = RowLayout.Centered
            viewport = ViewPortSize.Console120
            borders(borderColour = Colour.Green, bottomBorder = borderChar, topBorder = borderChar)
        }
        val staticCell1 = text1.toCell()
        val staticCell2 = text2.toCell()
        val row = PrettyRow(options, staticCell1, staticCell2)
        val render = row.render()
        render.output(enableOutput)
        val lines = render.lines()
        val separatorTotalLength = options.cellSeparator.size
        val projectedSize = row.planner.leftMargin + text1.length + text2.length + separatorTotalLength
        assertTrue(row.areOptionsExplicit)
        assertEquals(3, lines.size)
        assertEquals(projectedSize, lines[0].lengthNoAnsi)
        assertEquals(projectedSize, lines[1].length)
        assertEquals(projectedSize, lines[2].lengthNoAnsi)
        assertTrue { lines[0].contains(borderChar) }
        assertTrue { lines[2].contains(borderChar) }
    }

}