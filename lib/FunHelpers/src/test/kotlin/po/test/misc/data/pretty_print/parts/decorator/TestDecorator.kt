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
import po.misc.data.styles.SpecialChars
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf
import po.test.misc.data.pretty_print.setup.PrettyTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestDecorator: PrettyTest<TestDecorator>(enableOutput =  true){

    override val receiverType : TypeToken<TestDecorator> = tokenOf()
    private val decorator = Decorator{
        policy = DecorationPolicy.MandatoryGap
        verbosity = Verbosity.Debug
        addSeparator("*".toTaggedSeparator(BorderPosition.Top))
        addSeparator("-".toTaggedSeparator(BorderPosition.Bottom))
        addSeparator("|".toTaggedSeparator(BorderPosition.Right))
        addSeparator("|".toTaggedSeparator(BorderPosition.Left))
    }

    private val line1 = "Line 1"
    private val line2 = "Line 2"
    private val contentMaxLen = maxOf(line1.length, line2.length)

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
        decoration.output(enableOutput)
        val projectedLength = contentMaxLen + 2
        assertEquals(4, decoration.render.plain.linesCount)
        assertEquals(projectedLength, decoration.renderedLines.first().plainLength)
        assertEquals(projectedLength, decoration.renderedLines.second().plainLength)
        assertEquals(projectedLength, decoration.renderedLines.last().plainLength)
    }

    @Test
    fun `Decoration of content with virtual container size larger than content`(){
        val desiredWidth = contentMaxLen + 4
        val metrics = Decorator.Metrics(contentMaxLen, desiredWidth)
        val decoration = decorator.decorate(metrics, line1, line2)
        decoration.output(enableOutput)
        assertEquals(4, decoration.render.plain.linesCount)
        assertEquals(desiredWidth, decoration.renderedLines.first().plainLength, "Top line length is wrong")
        assertEquals(desiredWidth, decoration.renderedLines.second().plainLength, "First content line length is wrong")
        assertEquals(desiredWidth, decoration.renderedLines.last().plainLength, "Bottom line length is wrong")
    }

    @Test
    fun `Decoration of content with left offset`(){
        val leftOffset = 2
        val metrics = Decorator.Metrics(contentMaxLen, leftOffset =  leftOffset)
        val projectedSize =  contentMaxLen + 2 + leftOffset
        val decoration = decorator.decorate(metrics, line1, line2)
        decoration.output(enableOutput)
        assertEquals(4, decoration.render.plain.linesCount)
        assertEquals(projectedSize, decoration.renderedLines.first().plainLength, "Top line length is wrong")
        assertEquals(projectedSize, decoration.renderedLines.second().plainLength, "First content line length is wrong")
        assertEquals(projectedSize, decoration.renderedLines.last().plainLength, "Bottom line length is wrong")
    }

    @Test
    fun `Decoration of content with virtual container size larger than content and left offset`(){
        val leftOffset = 2
        val leftRightBorderSize = 2
        val containerSize = contentMaxLen + 2
        val projectedSize =  containerSize + leftRightBorderSize + leftOffset

        val metrics = Decorator.Metrics(contentMaxLen, containerSize, leftOffset)
        val decoration = decorator.decorate(metrics, line1, line2)
        decoration.output(enableOutput)
        assertEquals(4, decoration.render.plain.linesCount)
        assertEquals(projectedSize, decoration.renderedLines.first().plainLength, "Top line length is wrong")
        assertEquals(projectedSize, decoration.renderedLines.second().plainLength, "First content line length is wrong")
        assertEquals(projectedSize, decoration.renderedLines.last().plainLength, "Bottom line length is wrong")
    }

    @Test
    fun `Decorator policy MandatoryGap enforces presence of vertical gap`(){
        val text = "Some text"
        val decorator = Decorator{
            policy = DecorationPolicy.MandatoryGap
        }
        val decoration =  decorator.decorate(text)
        decoration.output(enableOutput)
        assertEquals(1, decorator[BorderPosition.Top].repeat)
        assertTrue("Top border disabled") { decorator[BorderPosition.Top].enabled }
        assertTrue { decoration.render.styled.contains(text) }
        assertEquals(decoration.renderedLines.first().plain, SpecialChars.NEW_LINE)
        assertEquals(text, decoration.renderedLines.second().plain)
    }

}