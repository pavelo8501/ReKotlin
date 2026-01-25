package po.test.misc.data.pretty_print.parts


import po.misc.data.logging.Verbosity
import po.misc.data.output.output
import po.misc.data.pretty_print.PrettyGrid
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.cells.PrettyCell
import po.misc.data.pretty_print.parts.options.Align
import po.misc.data.pretty_print.parts.options.Options
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.parts.options.RowOptions
import po.misc.data.pretty_print.parts.options.ViewPortSize
import po.misc.data.pretty_print.parts.rows.ValueRenderNode
import po.misc.data.pretty_print.parts.rows.BoundRenderNode
import po.misc.data.pretty_print.parts.rows.RowRenderPlanner
import po.misc.data.pretty_print.parts.rows.StaticRenderNode
import po.misc.data.pretty_print.parts.rows.Layout
import po.misc.data.styles.TextStyler
import po.misc.reflection.displayName
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf
import po.test.misc.data.pretty_print.setup.PrettyTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestRowRenderPlanner : PrettyTest<TestRowRenderPlanner>(enableOutput = true), TextStyler {

    override val receiverType: TypeToken<TestRowRenderPlanner> = tokenOf()

    private val text1 = "Text"
    private val text2 = "Text 2 bit longer"
    private val text3 = "Text 3 longer than text 2"
    private val text4 = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt"


    private val longText1 = "Text1 long enough"
    private val longText2 = "Text1 Lorem ipsum dolor sit amet, consectetur adipiscing"
    private val longText3 = "Text2 Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt"

    private val staticText1 = "Static"
    private val textForPretty = "Pretty text"
    private val textForKeyed = "Text 3"
    private val textForComputed = "Text 3"

    private val staticCell1 = staticText1.toCell()
    private val prettyCell = PrettyCell()
    private val keyedCell = TestRowRenderPlanner::textForKeyed.toCell()
    private val computedCell = TestRowRenderPlanner::textForComputed.toCell {}

    @Test
    fun `Rendering parameter creation logic`() {
        val options = RowOptions(Orientation.Horizontal, Layout.Stretch)
        val row = buildRow(){ applyOptions(options) }
        val planner = RowRenderPlanner(row)

        val parameters = planner.createRenderNodes(listOf(staticCell1, prettyCell, keyedCell, computedCell))
        assertNotNull(parameters.getOrNull(0)) { firstParam ->
            assertIs<StaticRenderNode>(firstParam)
        }
        assertNotNull(parameters.getOrNull(1)) { secondParam ->
            assertIs<ValueRenderNode>(secondParam)
        }
        assertNotNull(parameters.getOrNull(2)) { thirdParam ->
            assertIs<BoundRenderNode<TestRowRenderPlanner>>(thirdParam)
        }
        assertNotNull(parameters.getOrNull(3)) { fourthParam ->
            assertIs<BoundRenderNode<TestRowRenderPlanner>>(fourthParam)
        }
        assertEquals(4, parameters.size)
    }

    @Test
    fun `Rendering different cell types`() {
        val opts = RowOptions(Orientation.Horizontal, Layout.Compact)
        val row = buildRow{ applyOptions(opts) }
        val planner = RowRenderPlanner(row)
        var parameters =  planner.createRenderNodes(listOf(staticCell1))
        val staticNode =  assertIs<StaticRenderNode>(parameters.first())
        staticNode.render(Unit)
        assertEquals(staticText1.length, staticNode.contentWidth, "Wrong width staticNode")
        assertEquals(staticText1.length, planner.contentWidth, "StaticNode width does not influence rowWidth")

        parameters =  planner.createRenderNodes(listOf(prettyCell))
        val prettyNode =  assertIs<ValueRenderNode>(parameters.first())
        prettyNode.render(textForPretty)
        assertEquals(textForPretty.length, prettyNode.contentWidth, "Wrong width prettyNode")
        assertEquals(textForPretty.length, planner.contentWidth, "PrettyNode width does not influence rowWidth")

        val separatorLength = keyedCell.currentRenderOpts.keySeparator.toString().length
        val projectedWidth = textForKeyed.length + ::textForKeyed.displayName.length + separatorLength

        parameters =  planner.createRenderNodes(listOf(keyedCell))
        val keyedNode =  assertIs<BoundRenderNode<TestRowRenderPlanner>>(parameters.first())
        keyedNode.render(this)
        assertEquals(projectedWidth, keyedNode.contentWidth, "Wrong width keyedNode")
        assertEquals(projectedWidth, planner.contentWidth, "KeyedNode width does not influence rowWidth")
    }

    @Test
    fun `Rendering parameter selection for source aware`() {
        val opts = RowOptions(Orientation.Horizontal, Layout.Stretch)
        val row = buildRow{ applyOptions(opts) }
        val planner = RowRenderPlanner(row)
        val parameters = planner.createRenderNodes(listOf(staticCell1, prettyCell, keyedCell, computedCell))
        val awareParam = assertIs<BoundRenderNode<*>>(parameters.getOrNull(2))
        val selected = planner.checkSourceAware(this, awareParam)
        assertNotNull(selected)
    }

    @Test
    fun `RowRenderPlanner with cell's justify functionality  Align Left`() {

        val options = RowOptions(Orientation.Horizontal, Layout.Stretch, ViewPortSize.Console120)

        val keyed1 = ::text1.toCell()
        val static2 = text2.toCell()
        val row = PrettyRow<TestRowRenderPlanner>(opts =  options,  listOf(keyed1, static2))
        assertEquals(options, row.options)
        val rendered = row.render(this)
        rendered.output(enableOutput)
        assertEquals(options.viewport?.size, rendered.lengthNoAnsi)
    }

    @Test
    fun `RowRenderPlanner with cell's justify functionality Align Center`() {
        val options = Options(Align.Center)
        val rowOptions = RowOptions(Orientation.Horizontal, Layout.Stretch, ViewPortSize.Console120)
        val keyed1 = ::text1.toCell(options)
        val static2 = text2.toCell(options)
        val row = PrettyRow<TestRowRenderPlanner>(rowOptions,  listOf(keyed1, static2))
        val rendered = row.render(this)
        rendered.output(enableOutput)
        assertEquals(rowOptions.viewport?.size, rendered.length)
    }

    @Test
    fun `RowRenderPlanner with cell's justify functionality Align Right`() {
        val options = Options(Align.Right)
        val rowOptions = RowOptions(Orientation.Horizontal, Layout.Stretch, ViewPortSize.Console120)
        val static1 = text1.toCell(options)
        val static2 = text2.toCell(options)
        listOf(static1, static2)
        val row = PrettyRow<TestRowRenderPlanner>(rowOptions,  listOf(static1, static2))
        val rendered = row.renderAny()
        rendered.output(enableOutput)
        assertEquals(rowOptions.viewport?.size, rendered.length)
    }

    @Test
    fun `Cell width computation for Layout Stretch`() {
        val options = RowOptions(Orientation.Horizontal, Layout.Stretch)
        val row = buildRow{ applyOptions(options) }
        val planner = RowRenderPlanner(row)
        val nodes = planner.createRenderNodes(listOf(staticCell1, prettyCell, keyedCell, computedCell))
        val maxWidth = options.viewport?.size?:0
        val cellWidth = maxWidth / 4
        val firsNode =  assertIs<StaticRenderNode>(nodes.getOrNull(0))
        val secondNode =  assertIs<ValueRenderNode>(nodes.getOrNull(1))
        val rendered = firsNode.render(Unit)
        rendered.output(enableOutput)
        assertEquals(cellWidth, firsNode.contentWidth, "FirsNode mutated width")
        assertEquals(cellWidth, secondNode.contentWidth, "SecondNode mutated width")
        assertEquals(cellWidth, rendered.plainLength)

    }

    @Test
    fun `Inner border calculation`(){
        val options = RowOptions(Orientation.Horizontal)
        val row = buildRow{ applyOptions(options) }
        val planner = RowRenderPlanner(row)
        val keyed1 = ::text1.toCell()
        val keyed2 = ::text2.toCell()
        val keyed3 = ::text3.toCell()
        val keyed4 = ::text4.toCell()

        planner.createRenderNodes(listOf(keyed1))
        assertFalse("Single node should have no borders") { planner.nodes[0].innerBorders.bordersEnabled }

        planner.createRenderNodes(listOf(keyed1, keyed2))
        assertTrue("Last node should have left border") { planner.nodes[1].innerBorders.leftBorder.enabled }

        planner.createRenderNodes(listOf(keyed1, keyed2, keyed3, keyed4))
        assertFalse("First node should no have borders") { planner.nodes[0].innerBorders.bordersEnabled }
        assertTrue("Second node should have borders") { planner.nodes[1].innerBorders.bordersEnabled }
        assertTrue("Third node should have borders") { planner.nodes[2].innerBorders.bordersEnabled }
        assertTrue("Last node should have left border") { planner.nodes[3].innerBorders.leftBorder.enabled }
    }

    @Test
    fun `Row shaping logic, only last cell should be trimmed`(){
        val options = RowOptions(Orientation.Horizontal, ViewPortSize.Console80)
        val keyed1 = ::longText1.toCell()
        val keyed2 = ::longText2.toCell()
        val cellList = listOf(keyed1, keyed2)
        val row = PrettyRow<TestRowRenderPlanner>(options, cells = cellList)
        if(enableOutput){
            row.verbosity = Verbosity.Debug
        }
        val render = row.render(this)
        render.output(enableOutput)
        assertEquals(ViewPortSize.Console80.size, render.lengthNoAnsi)
    }

    @Test
    fun `Row shaping logic, both cells should be trimmed`(){

        val options = RowOptions(Orientation.Horizontal, ViewPortSize.Console80)
        val keyed2 = ::longText2.toCell()
        val keyed3 = ::longText3.toCell()
        val cellList = listOf(keyed2, keyed3)
        val row = PrettyRow<TestRowRenderPlanner>(options, cells = cellList)
        if(enableOutput){
            row.verbosity = Verbosity.Debug
        }
        val render = row.render(this)
        render.output(enableOutput)
        assertEquals(ViewPortSize.Console80.size, render.lengthNoAnsi)
    }

    @Test
    fun `Row obey imposed size constraints`(){
        val options = RowOptions(Orientation.Horizontal, ViewPortSize.Console180)
        val gridOptions = RowOptions(Orientation.Horizontal, ViewPortSize.Console80)
        val keyed1 = ::longText3.toCell()
        val cellList = listOf(keyed1)
        val row = PrettyRow<TestRowRenderPlanner>(options, cells = cellList)
        if(enableOutput){
            row.verbosity = Verbosity.Debug
        }
        val planner = row.planner
        val grid = PrettyGrid<TestRowRenderPlanner>(opts = gridOptions)
        grid.addRow(row)
        assertEquals(ViewPortSize.Console80.size, planner.maxWidth)
        val render = row.render(this)
        render.output(enableOutput)
        assertEquals(ViewPortSize.Console80.size, render.lengthNoAnsi)
    }

}
