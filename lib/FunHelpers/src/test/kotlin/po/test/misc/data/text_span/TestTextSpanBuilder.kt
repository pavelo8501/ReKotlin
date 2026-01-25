package po.test.misc.data.text_span

import po.misc.data.output.output
import po.misc.data.styles.TextStyler
import po.misc.data.text_span.TextSpan
import po.misc.data.text_span.buildTextSpan
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestTextSpanBuilder: TextStyler {

    private enum class SomeEnum { Value1, Value2 }
    private val someEnum :SomeEnum = SomeEnum.Value1
    private val someEnumList = listOf(SomeEnum.Value1, SomeEnum.Value2)
    private val listOfStrings = listOf<String>("String 1", "String 2", "String 3" )

    @Test
    fun `Builder usage with known classes`(){
        val textSpan: TextSpan = buildTextSpan {
            append(::someEnum)
        }
        assertFalse { textSpan.plain.isStyled }
        assertTrue{ textSpan.styled.isStyled }
        textSpan.output()
    }
//
//    @Test
//    fun `AppendLine with single value`(){
//        val textSpan = buildTextSpan {
//            appendLine(someEnumList)
//        }
//        val plainLines = textSpan.plain.lines()
//        val styledLines = textSpan.plain.lines()
//        assertEquals(2, plainLines.size)
//        assertEquals(2, styledLines.size)
//    }
//    @Test
//    fun `AppendLine with lists`(){
//        val textSpan = buildTextSpan {
//            appendLine(listOfStrings)
//        }
//        val plainLines = textSpan.plain.lines()
//        val styledLines = textSpan.plain.lines()
//        assertEquals(3, plainLines.size)
//        assertEquals(3, styledLines.size)
//    }
//    @Test
//    fun `AppendLine with property single value`(){
//        val textSpan = buildTextSpan {
//            appendLine(::someEnum)
//        }
//        textSpan.output()
//    }
//    @Test
//    fun `AppendLine with properties`(){
//        val textSpan = buildTextSpan {
//            appendLine(::someEnumList)
//        }
//        textSpan.output()
//        val plainLines = textSpan.plain.lines()
//        val styledLines = textSpan.styled.lines()
//        assertEquals(2, plainLines.size)
//        assertEquals(2, styledLines.size)
//    }
}