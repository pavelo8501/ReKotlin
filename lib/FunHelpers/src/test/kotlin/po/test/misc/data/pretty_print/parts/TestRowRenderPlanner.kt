package po.test.misc.data.pretty_print.parts


import po.misc.data.helpers.lengthNoAnsi
import po.misc.data.logging.Verbosity
import po.misc.data.output.output
import po.misc.data.pretty_print.PrettyGrid
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.Templated
import po.misc.data.pretty_print.cells.KeyedCell
import po.misc.data.pretty_print.cells.PrettyCell
import po.misc.data.pretty_print.cells.PrettyCellBase
import po.misc.data.pretty_print.parts.options.Align
import po.misc.data.pretty_print.parts.options.ExtendedString
import po.misc.data.pretty_print.parts.options.Options
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.parts.options.RowOptions
import po.misc.data.pretty_print.parts.options.ViewPortSize
import po.misc.data.pretty_print.parts.rendering.ValueRenderNode
import po.misc.data.pretty_print.parts.rendering.BoundRenderNode
import po.misc.data.pretty_print.parts.rendering.RowRenderPlanner
import po.misc.data.pretty_print.parts.rendering.StaticRenderNode
import po.misc.data.pretty_print.parts.rows.RowLayout
import po.misc.data.strings.FormattedText
import po.misc.data.strings.createFormatted
import po.misc.data.styles.TextStyler
import po.misc.data.styles.contains
import po.misc.reflection.displayName
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf
import po.test.misc.data.pretty_print.setup.PrettyTest
import kotlin.reflect.KProperty
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class TestRowRenderPlanner : PrettyTest<TestRowRenderPlanner>(enableOutput = false), TextStyler {

    override val type: TypeToken<TestRowRenderPlanner> = tokenOf()

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
        val planner = RowRenderPlanner(type)
        val options = RowOptions(Orientation.Horizontal, RowLayout.Stretch)
        val parameters = planner.createRenderNodes(listOf(staticCell1, prettyCell, keyedCell, computedCell), options)
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
        val opts = RowOptions(Orientation.Horizontal, RowLayout.Compact)
        val planner = RowRenderPlanner(type)
        var parameters =  planner.createRenderNodes(listOf(staticCell1), opts)
        val staticNode =  assertIs<StaticRenderNode>(parameters.first())
        staticNode.render()
        assertEquals(staticText1.length, staticNode.width, "Wrong width staticNode")
        assertEquals(staticText1.length, planner.rowWidth, "StaticNode width does not influence rowWidth")

        parameters =  planner.createRenderNodes(listOf(prettyCell), opts)
        val prettyNode =  assertIs<ValueRenderNode>(parameters.first())
        prettyNode.render(textForPretty)
        assertEquals(textForPretty.length, prettyNode.width, "Wrong width prettyNode")
        assertEquals(textForPretty.length, planner.rowWidth, "PrettyNode width does not influence rowWidth")

        val separatorLength = keyedCell.currentRenderOpts.keySeparator.toString().length
        val projectedWidth = textForKeyed.length + ::textForKeyed.displayName.length + separatorLength

        parameters =  planner.createRenderNodes(listOf(keyedCell), opts)
        val keyedNode =  assertIs<BoundRenderNode<TestRowRenderPlanner>>(parameters.first())
        keyedNode.render(this)
        assertEquals(projectedWidth, keyedNode.width, "Wrong width keyedNode")
        assertEquals(projectedWidth, planner.rowWidth, "KeyedNode width does not influence rowWidth")
    }

    @Test
    fun `Rendering parameter selection for source aware`() {
        val opts = RowOptions(Orientation.Horizontal, RowLayout.Stretch)
        val planner = RowRenderPlanner(type)
        val parameters = planner.createRenderNodes(listOf(staticCell1, prettyCell, keyedCell, computedCell), opts)
        val awareParam = assertIs<BoundRenderNode<*>>(parameters.getOrNull(2))
        val selected = planner.checkSourceAware(this, awareParam)
        assertNotNull(selected)
    }

    @Test
    fun `RowRenderPlanner with cell's justify functionality  Align Left`() {
        val options = RowOptions(Orientation.Horizontal, RowLayout.Stretch, ViewPortSize.Console120)
        val keyed1 = ::text1.toCell()
        val static2 = text2.toCell()
        val row = PrettyRow<TestRowRenderPlanner>(options,  listOf(keyed1, static2))
        val rendered = row.render(this)
        rendered.output(enableOutput)
        assertEquals(options.viewport.size, rendered.lengthNoAnsi)
    }

    @Test
    fun `RowRenderPlanner with cell's justify functionality Align Center`() {
        val options = Options(Align.Center)
        val rowOptions = RowOptions(Orientation.Horizontal, RowLayout.Stretch, ViewPortSize.Console120)
        val keyed1 = ::text1.toCell(options)
        val static2 = text2.toCell(options)
        val row = PrettyRow<TestRowRenderPlanner>(rowOptions,  listOf(keyed1, static2))
        val rendered = row.render(this)
        rendered.output(enableOutput)
        assertEquals(rowOptions.viewport.size, rendered.length)
    }

    @Test
    fun `RowRenderPlanner with cell's justify functionality Align Right`() {
        val options = Options(Align.Right)
        val rowOptions = RowOptions(Orientation.Horizontal, RowLayout.Stretch, ViewPortSize.Console120)
        val static1 = text1.toCell(options)
        val static2 = text2.toCell(options)
        listOf(static1, static2)
        val row = PrettyRow<TestRowRenderPlanner>(rowOptions,  listOf(static1, static2))
        val rendered = row.renderAny()
        rendered.output(enableOutput)
        assertEquals(rowOptions.viewport.size, rendered.length)
    }

    @Test
    fun `Cell width computation for RowLayout Stretch`() {
        val options = RowOptions(Orientation.Horizontal, RowLayout.Stretch)
        val planner = RowRenderPlanner(type)
        val nodes = planner.createRenderNodes(listOf(staticCell1, prettyCell, keyedCell, computedCell), options)
        val maxWidth = options.viewport.size
        val cellWidth = maxWidth / 4
        val firsNode =  assertIs<StaticRenderNode>(nodes.getOrNull(0))
        val secondNode =  assertIs<ValueRenderNode>(nodes.getOrNull(1))
        val rendered = firsNode.render()
        rendered.output(enableOutput)
        assertEquals(cellWidth, firsNode.width, "FirsNode mutated width")
        assertEquals(cellWidth, secondNode.width, "SecondNode mutated width")
        assertEquals(cellWidth, rendered.length)

    }

    @Test
    fun `Inner border calculation`(){
        val options = RowOptions(Orientation.Horizontal)
        val planner = RowRenderPlanner(type)
        val keyed1 = ::text1.toCell()
        val keyed2 = ::text2.toCell()
        val keyed3 = ::text3.toCell()
        val keyed4 = ::text4.toCell()

        planner.createRenderNodes(listOf(keyed1), options)
        assertFalse("Single node should have no borders") { planner.nodes[0].borders.bordersEnabled }

        planner.createRenderNodes(listOf(keyed1, keyed2), options)
        assertTrue("Last node should have left border") { planner.nodes[1].borders.leftBorder.enabled }

        planner.createRenderNodes(listOf(keyed1, keyed2, keyed3, keyed4), options)
        assertFalse("First node should no have borders") { planner.nodes[0].borders.bordersEnabled }
        assertTrue("Second node should have borders") { planner.nodes[1].borders.bordersEnabled }
        assertTrue("Third node should have borders") { planner.nodes[2].borders.bordersEnabled }
        assertTrue("Last node should have left border") { planner.nodes[3].borders.leftBorder.enabled }
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
        val options = RowOptions(Orientation.Horizontal, ViewPortSize.Console220)
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
        assertEquals(ViewPortSize.Console80.size, planner.parameters.declaredWidth)
        val render = row.render(this)
        render.output(enableOutput)
        assertEquals(ViewPortSize.Console80.size, render.lengthNoAnsi)
    }

}
