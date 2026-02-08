package po.test.misc.data.text_span

import org.junit.jupiter.api.assertAll
import po.misc.collections.second
import po.misc.data.styles.Colour
import po.misc.data.styles.TextStyler
import po.misc.data.styles.colorize
import po.misc.data.text_span.OrderedText
import po.misc.data.text_span.append
import po.misc.data.text_span.appendCreating
import po.misc.data.text_span.mergeTexts
import po.misc.testing.assertBlock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotSame
import kotlin.test.assertTrue

class TestOrderedText: TextStyler {

    @Test
    fun `Construction from strings`(){
        val text = OrderedText("Line", "Line")
        assertEquals(2, text.linesCount)
    }

    @Test
    fun `Construction from spans`(){
        val span1 = "Line 1".colorize(Colour.Magenta).toPair()
        val span2 = "Line 2".colorize(Colour.Cyan).toPair()
        val text = OrderedText(span1, span2)
        val line1 =  text.lines.first()
        val line2 =  text.lines.second()
        assertEquals(2, text.linesCount)

        line1.assertBlock("Line 1"){
            assertEquals(span1.plain, plain)
            assertFalse(plain.isStyled)
            assertTrue(styled.isStyled)
        }

        line2.assertBlock("Line 2"){
            assertEquals(span2.plain, plain)
            assertFalse(plain.isStyled)
            assertTrue(styled.isStyled)
        }
    }

    @Test
    fun `Append string`(){
        val initial = "Some text".toPair()
        val text = OrderedText(initial)
        val other = "Other text"
        text appendCreating other
        assertEquals(2, text.linesCount)
        assertEquals(initial.plain, text.lines.first().plain)
        assertEquals(other, text.lines.second().plain)
    }

    @Test
    fun `Append span`(){
        val initial = "Some text".toPair()
        val text = OrderedText(initial)
        val other = "Other text".toPair()
        text append other
        assertEquals(2, text.linesCount)
        assertEquals(initial.plain, text.lines.first().plain)
        assertEquals(other.plain, text.lines.second().plain)
    }

    @Test
    fun `Appending text`(){
        val text1 = OrderedText("Text1")
        val text2 = OrderedText("Text2")
        text1.append(text2)

        assertEquals(2, text1.linesCount)
        val line1 =  text1.lines.first()
        val line2 =  text1.lines.second()

        line1.assertBlock("Line 1"){
            assertEquals("Text1", plain)
        }
        line2.assertBlock("Line 1"){
            assertEquals("Text2", plain)
        }
    }

    @Test
    fun `Merging text line by line`(){
        val text1 = OrderedText("Line", "Line")
        val text2 = OrderedText("1", "2")
        text1.merge(text2)
        assertEquals(2, text1.linesCount)
        val line1 = text1.lines.first()
        val line2 = text1.lines.second()

        line1.assertBlock("Line 1"){
            assertEquals("Line 1", plain)
            assertFalse(hasLineBreak)
        }
        line2.assertBlock("Line 2"){
            assertEquals("Line 2", plain)
            assertFalse(hasLineBreak)
        }
    }

    @Test
    fun `Merging OrderedTexts into new text`(){
        val text1 = OrderedText("Line", "Line")
        val text2 = OrderedText("1", "2")
        val list = listOf(text1, text2)
        val merged = list.mergeTexts()
        val line1 = merged.lines.first()
        val line2 = merged.lines.second()

        assertNotSame(merged, line1)
        assertNotSame(merged, line2)

        line1.assertBlock("Line 1"){
            assertEquals("Line 1", plain)
            assertFalse(hasLineBreak)
        }
        line2.assertBlock("Line 2"){
            assertEquals("Line 2", plain)
            assertFalse(hasLineBreak)
        }
    }

}