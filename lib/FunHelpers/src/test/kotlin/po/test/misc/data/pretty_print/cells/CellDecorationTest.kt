package po.test.misc.data.pretty_print.cells

import po.misc.collections.asList
import po.misc.data.output.output
import po.misc.data.pretty_print.parts.decorator.BorderPosition
import po.misc.data.pretty_print.parts.decorator.Decorator
import po.misc.data.pretty_print.parts.options.BorderPresets
import po.misc.data.pretty_print.parts.options.Options
import po.misc.data.pretty_print.parts.render.CellParameters
import po.misc.data.pretty_print.parts.render.LayerType
import po.misc.data.pretty_print.parts.render.RenderCanvas
import po.misc.testing.assertBlock
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf
import po.test.misc.data.pretty_print.setup.PrettyTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CellDecorationTest: PrettyTest<CellDecorationTest>(), CellParameters {

    override val receiverType: TypeToken<CellDecorationTest> = tokenOf()

    override var maxWidth : Int = 0
    override var leftOffset : Int = 0

    private val shortText = "Text 1"
    private val shortText2 = "Text 2"
    private val cellOption = Options {
        borders(BorderPresets.HalfBox)
    }

    @Test
    fun `Compact cell`() {
        val expectedSize = shortText.length
        val cell1 = shortText.toCell(cellOption)
        val render = with(cell1) {
            renderInScope()
        }
        val lines = render.plain.lines()
        assertEquals(3, lines.size)
        assertEquals(expectedSize, lines[0].length)
        assertEquals(expectedSize, lines[1].length)
        assertEquals(expectedSize, lines[2].length)
    }

    @Test
    fun `Cell decoration with upper containers left border`() {
        var expectedSize = shortText.length
        val cell1 = shortText.toCell(cellOption)
        val cellCanvas = RenderCanvas(LayerType.Dynamic)
        with(cell1) {
            val span = renderInScope()
            cellCanvas.addSpan(span)
        }
        val canvas = RenderCanvas(LayerType.Decoration)
        canvas.mergeAllToActiveLayer(cellCanvas.asList())
        assertNotNull(canvas.layers.firstOrNull()){layer->
            assertTrue("Layer disabled") { layer.enabled }
            assertEquals(LayerType.Decoration, layer.layerType, "Wrong layer type")
            assertEquals(3, layer.lines.size, "Wrong layer lines count")
            assertEquals(expectedSize, layer.lineMaxLen, "Line size wrong")
        }
        expectedSize += 1
        val decorator = Decorator{
            verbosity = testVerbosity
            addSeparator("|".toSeparator(BorderPosition.Left))
        }
        val decoration =  decorator.decorate(canvas)
        decoration.layer.output()
        decoration.layer.assertBlock("Decorated layer"){
            assertTrue("Layer disabled") { enabled }
            assertEquals(LayerType.Decoration, layerType, "Wrong layer type")
            assertEquals(3, lines.size, "Wrong layer lines count")
            assertEquals(expectedSize, lineMaxLen, "Line size wrong")
        }
    }

    @Test
    fun `Cell decoration with upper containers left and right border`() {
        val cell1 = shortText.toCell(cellOption)
        val cellCanvas = RenderCanvas(LayerType.Dynamic)
        with(cell1) {
            cellCanvas.addSpan(renderInScope())
        }
        val canvas = RenderCanvas(LayerType.Decoration)
        canvas.mergeAllToActiveLayer(cellCanvas.asList())
        val decorator = Decorator{
            verbosity = testVerbosity
            addSeparator("|".toSeparator(BorderPosition.Left), "|".toSeparator(BorderPosition.Right))
        }
        val expectedSize = shortText.length + 2
        val decoration =  decorator.decorate(canvas)
        decoration.layer.assertBlock("Decorated layer"){
            assertTrue("Layer disabled") { enabled }
            assertEquals(LayerType.Decoration, layerType, "Wrong layer type")
            assertEquals(3, lines.size, "Wrong layer lines count")
            assertEquals(expectedSize, lineMaxLen, "Line size wrong")
        }
    }

    @Test
    fun `Cell decoration with upper containers top border`() {
        val expectedSize = shortText.length
        val expectedLinesCount = 4
        val cell1 = shortText.toCell(cellOption)
        val cellCanvas = RenderCanvas(LayerType.Dynamic)
        with(cell1) {
            val span = renderInScope()
            cellCanvas.addSpan(span)
        }
        val canvas = RenderCanvas(LayerType.Decoration)
        canvas.mergeAllToActiveLayer(cellCanvas.asList())
        val decorator = Decorator{
            verbosity = testVerbosity
            addSeparator("~".toSeparator(BorderPosition.Top))
        }
        val decoration =  decorator.decorate(canvas)
        decoration.layer.assertBlock("Decorated layer"){
            assertTrue("Layer disabled") { enabled }
            assertEquals(LayerType.Decoration, layerType, "Wrong layer type")
            assertEquals(expectedLinesCount, lines.size, "Wrong layer lines count")
            assertEquals(expectedSize, lineMaxLen, "Line size wrong")
        }
    }

    @Test
    fun `Cell decoration with upper containers top and bottom border`() {
        val expectedSize = shortText.length
        val expectedLinesCount = 5
        val cell1 = shortText.toCell(cellOption)
        val cellCanvas = RenderCanvas(LayerType.Dynamic)
        with(cell1) {
            val span = renderInScope()
            cellCanvas.addSpan(span)
        }
        val canvas = RenderCanvas(LayerType.Decoration)
        canvas.mergeAllToActiveLayer(cellCanvas.asList())
        val decorator = Decorator{
            verbosity = testVerbosity
            addSeparator("~".toSeparator(BorderPosition.Top), "~".toSeparator(BorderPosition.Bottom))
        }
        val decoration =  decorator.decorate(canvas)
        decoration.layer.assertBlock("Decorated layer"){
            assertTrue("Layer disabled") { enabled }
            assertEquals(LayerType.Decoration, layerType, "Wrong layer type")
            assertEquals(expectedLinesCount, lines.size, "Wrong layer lines count")
            assertEquals(expectedSize, lineMaxLen, "Line size wrong")
        }
    }

    @Test
    fun `Cell decoration with upper containers size larger than content`() {
        maxWidth = 40
        val expectedLinesCount = 5
        val cell1 = shortText.toCell(cellOption)
        val cellCanvas = RenderCanvas(LayerType.Dynamic)
        with(cell1) {
            cellCanvas.addSpan(renderInScope())
        }
        val canvas = RenderCanvas(LayerType.Decoration)
        canvas.mergeAllToActiveLayer(cellCanvas.asList())
        val decorator = Decorator{
            verbosity = testVerbosity
            addSeparator("~".toSeparator(BorderPosition.Top), "~".toSeparator(BorderPosition.Bottom))
        }
        val decoration = decorator.decorate(canvas, this)
        assertEquals(maxWidth, decoration.snapshot.metrics.maxWidth)
        decoration.layer.assertBlock("Decorated layer"){
            assertTrue("Layer disabled") { enabled }
            assertEquals(LayerType.Decoration, layerType, "Wrong layer type")
            assertEquals(expectedLinesCount, lines.size, "Wrong layer lines count")
            assertEquals(maxWidth, lineMaxLen, "Line size wrong")
        }
    }

    @Test
    fun `Cell decoration with upper containers size larger than content and left offset`() {
        maxWidth = 40
        leftOffset = 10
        val expectedLinesCount = 5
        val cell1 = shortText.toCell(cellOption)
        val cellCanvas = RenderCanvas(LayerType.Dynamic)
        with(cell1) {
            cellCanvas.addSpan(renderInScope())
        }
        val canvas = RenderCanvas(LayerType.Decoration)
        canvas.mergeAllToActiveLayer(cellCanvas.asList())
        val top = "~".toSeparator(BorderPosition.Top)
        val bottom = "~".toSeparator(BorderPosition.Bottom)
        val left = "|".toSeparator(BorderPosition.Left)
       // val right = "|".toSeparator(BorderPosition.Left)
        val decorator = Decorator{
            verbosity = testVerbosity
            addSeparator(top, bottom, left)
        }
        val decoration = decorator.decorate(canvas, this)
        decoration.output(testVerbosity)

        assertEquals(maxWidth, decoration.snapshot.metrics.maxWidth)
        decoration.layer.assertBlock("Decorated layer"){
            assertTrue("Layer disabled") { enabled }
            assertEquals(LayerType.Decoration, layerType, "Wrong layer type")
            assertEquals(expectedLinesCount, lines.size, "Wrong layer lines count")
            assertEquals(maxWidth, lineMaxLen, "Line size wrong")
        }
    }

}
