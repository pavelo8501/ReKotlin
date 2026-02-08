package po.test.misc.data.strings

import po.misc.data.strings.appendStyled
import po.misc.data.strings.appendStyledLine
import po.misc.data.strings.contains
import po.misc.data.styles.Colour
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestStringBuilderExtensions {

    @Test
    fun `AppendStyledLine extension`(){
        val bool = true
        val string = buildString {
            appendLine("Text")
            appendStyledLine(bool)
            appendStyledLine(bool, Colour.Blue)
        }
        val lines = string.lines()
        assertEquals(4, lines.size)
        assertTrue { lines[0].contains("Text") }
        assertTrue { lines[1].contains("True") && lines[1].contains(Colour.Green) }
        assertTrue { lines[2].contains("true") && lines[2].contains(Colour.Blue) }
        assertEquals("", lines[3])
    }

    @Test
    fun `Append line extension`(){
        val bool = true
        val string = buildString {
            append("Text ")
            appendStyled(bool, " ")
            appendStyled(Colour.Blue, bool)
        }
        val split = string.split(' ')
        assertEquals(3, split.size)
        assertTrue { split[0].contains("Text") }
        assertTrue { split[1].contains("True") && split[1].contains(Colour.Green) }
        assertTrue { split[2].contains("true") && split[2].contains(Colour.Blue) }
    }

}