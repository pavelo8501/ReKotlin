package po.test.misc.data.pretty_print.parts.decorator

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import po.misc.collections.second
import po.misc.data.linesCount
import po.misc.data.logging.Verbosity
import po.misc.data.output.output
import po.misc.data.pretty_print.parts.decorator.BorderPosition
import po.misc.data.pretty_print.parts.decorator.DecorationPolicy
import po.misc.data.pretty_print.parts.decorator.Decorator
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.parts.render.CanvasLayer
import po.misc.data.pretty_print.parts.render.LayerType
import po.misc.data.pretty_print.parts.render.RenderCanvas
import po.misc.data.pretty_print.parts.render.RenderRole
import po.misc.data.strings.contains
import po.misc.data.styles.SpecialChars
import po.misc.data.text_span.OrderedText
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf
import po.test.misc.data.pretty_print.setup.PrettyTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestDecorator: PrettyTest<TestDecorator>(Verbosity.Debug){

    override val receiverType : TypeToken<TestDecorator> = tokenOf()
    private val decorator = Decorator{
        policy = DecorationPolicy.MandatoryGap
        verbosity = testVerbosity
        addSeparator("*".toSeparator(BorderPosition.Top))
        addSeparator("-".toSeparator(BorderPosition.Bottom))
        addSeparator("|".toSeparator(BorderPosition.Right))
        addSeparator("|".toSeparator(BorderPosition.Left))
    }

    private val line1 = "Line 1"
    private val line2 = "Line 2"
    private val contentMaxLen = maxOf(line1.length, line2.length)

    private fun createDecorationLayer(): CanvasLayer{
        val topDecoration = "--------------".toPair(RenderRole.TopBorder)
        val content = "Content".toPair(RenderRole.Content)
        val bottomDecoration = "--------------".toPair(RenderRole.BottomBorder)
        return CanvasLayer(LayerType.Decoration, listOf(topDecoration,content, bottomDecoration))
    }

    private fun createRenderLayer(postfix: String = ""): CanvasLayer{
        return CanvasLayer(LayerType.Render, listOf("Content ${postfix}_1".toPair()))
    }

    @BeforeEach
    fun checkBorders(){
        assertTrue("Decorator disabled") { decorator.enabled }
        assertTrue("Top decorator border disabled") { decorator.topBorder.enabled }
        assertTrue("Right decorator border disabled") { decorator.rightBorder.enabled }
        assertTrue("Left decorator border disabled") { decorator.leftBorder.enabled }
        assertTrue("Bottom decorator border disabled") { decorator.bottomBorder.enabled }
    }

    @Test
    fun `Decoration of base content`(){
        val decoration = decorator.decorate(line1, line2)
        decoration.output(testVerbosity)
        val projectedLength = contentMaxLen + 2
        assertEquals(4, decoration.layer.linesCount)
        assertEquals(projectedLength, decoration.lines.first().plainLength)
        assertEquals(projectedLength, decoration.lines.second().plainLength)
        assertEquals(projectedLength, decoration.lines.last().plainLength)
    }

    @Test
    fun `Decoration of content with virtual container size larger than content`(){
        val desiredWidth = contentMaxLen + 4
        val metrics = Decorator.Metrics(contentMaxLen, desiredWidth)
        val decoration = decorator.decorate(metrics, line1, line2)
        decoration.output(testVerbosity)
        assertEquals(4, decoration.layer.linesCount)
        assertEquals(desiredWidth, decoration.lines.first().plainLength, "Top line length is wrong")
        assertEquals(desiredWidth, decoration.lines.second().plainLength, "First content line length is wrong")
        assertEquals(desiredWidth, decoration.lines.last().plainLength, "Bottom line length is wrong")
    }

    @Test
    fun `Decoration of content with left offset`(){
        val leftOffset = 2
        val metrics = Decorator.Metrics(contentMaxLen, leftOffset =  leftOffset)
        val projectedSize =  contentMaxLen + 2 + leftOffset
        val decoration = decorator.decorate(metrics, line1, line2)
        decoration.output(testVerbosity)
        assertEquals(4, decoration.layer.linesCount)
        assertEquals(projectedSize, decoration.lines.first().plainLength, "Top line length is wrong")
        assertEquals(projectedSize, decoration.lines.second().plainLength, "First content line length is wrong")
        assertEquals(projectedSize, decoration.lines.last().plainLength, "Bottom line length is wrong")
    }

    @Test
    fun `Decoration of content with virtual container size larger than content and left offset`(){
        val leftOffset = 2
        val leftRightBorderSize = 2
        val containerSize = contentMaxLen + 2
        val projectedSize =  containerSize + leftRightBorderSize + leftOffset

        val metrics = Decorator.Metrics(containerSize, leftOffset)
        val decoration = decorator.decorate(metrics, line1, line2)
        decoration.output(testVerbosity)
        assertEquals(4, decoration.layer.linesCount)
        assertEquals(projectedSize, decoration.lines.first().plainLength, "Top line length is wrong")
        assertEquals(projectedSize, decoration.lines.second().plainLength, "First content line length is wrong")
        assertEquals(projectedSize, decoration.lines.last().plainLength, "Bottom line length is wrong")
    }

    @Test
    fun `Canvas active layer is properly processed`(){
        val line1 = "Line 1".toPair()
        val line2 = "Line 2".toPair()
        val line3 = "Line 3".toPair()
        val whitesPaceSize = 2
        val verticalBordersSize = 2
        val projectedSize = line1.plainLength + line2.plainLength + line3.plainLength + whitesPaceSize + verticalBordersSize
        val canvas = RenderCanvas(LayerType.Render, listOf(line1, line2, line3))
        val decoration = decorator.decorate(canvas)
        val content = decoration.layer[RenderRole.Content]
        assertEquals(projectedSize, content.plainLength)
    }

    @Test
    fun `Decorations correctly applied to normalized canvas`(){

        val line1 = "Line 1".toPair()
        val decoration = decorator.decorate(line1)

        val layer = decoration.layer
        assertEquals(Orientation.Vertical, decoration.layer.displayOrientation)
        assertEquals(3, decoration.layer.linesCount)
        assertEquals(RenderRole.TopBorder, layer.lines[0].role)
        assertEquals(RenderRole.Content, layer.lines[1].role)
        assertEquals(RenderRole.BottomBorder, layer.lines[2].role)

        val line2 = "Line 2".toPair()
        val text =  OrderedText(line2)
        val canvas = RenderCanvas(LayerType.Render, text.lines)
        canvas.activeLayer.changeRole(RenderRole.Content)
    }

    @Test
    fun `Decorations correctly applied to multi line canvas`(){
        val layer = createDecorationLayer()
        val canva = RenderCanvas(LayerType.Dynamic)
        canva.addLayer(layer)
        val decoration = decorator.decorate(canva)
        decoration.output()
    }

    @Test
    fun `Decorator policy MandatoryGap enforces presence of vertical gap`(){
        val text = "Some text"
        val decorator = Decorator{
            policy = DecorationPolicy.MandatoryGap
        }
        val decoration =  decorator.decorate(text)
        decoration.output(testVerbosity)
        assertEquals(1, decorator[BorderPosition.Top].repeat)
        assertTrue("Top border disabled") { decorator[BorderPosition.Top].enabled }
        assertTrue { decoration.layer.contains(text) }
        assertEquals(decoration.lines.first().plain, SpecialChars.NEW_LINE)
        assertEquals(text, decoration.lines.second().plain)
    }
}