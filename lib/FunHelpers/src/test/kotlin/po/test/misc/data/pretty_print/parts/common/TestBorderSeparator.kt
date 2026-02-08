package po.test.misc.data.pretty_print.parts.common

import po.misc.data.output.output
import po.misc.data.pretty_print.parts.decorator.BorderPosition
import po.misc.data.pretty_print.parts.decorator.DecoratorBorder
import po.misc.data.pretty_print.parts.common.Separator
import po.misc.data.strings.contains
import po.misc.data.styles.Colour
import po.misc.data.styles.TextStyler
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestBorderSeparator : TextStyler{

    @Test
    fun `Building scheme`(){
        val separator = DecoratorBorder(BorderPosition.Top){
            setScheme(" ", 20).append("|", 1)
        }
        val string =  separator.toString()
        string.output()
        assertEquals(21, string.length)
    }

    @Test
    fun `Building scheme from existent Extended String`(){
       // val asterix = BorderSeparator(BorderPosition.Top, "*", Colour.Magenta)
        val asterix = Separator("*", Colour.Magenta)
        val separator = DecoratorBorder(BorderPosition.Top){
            setScheme(asterix, 20).append("#", 3).append("|", Colour.RedBright)
        }
        val sting = separator.toString()
        sting.output()
        assertEquals(24, sting.lengthNoAnsi)
        assertTrue { sting.contains("*".repeat(20)) && sting.contains(Colour.Magenta) }
        assertTrue { sting.contains("#".repeat(3)) && sting.contains(Colour.Magenta) }
        assertTrue { sting.contains("|") && sting.contains(Colour.RedBright) }
    }

    @Test
    fun `Prepend functionality`(){
        val separator = DecoratorBorder(BorderPosition.Top, "*")
        separator.prepend("|", 5)
        assertEquals(2, separator.renderScheme.size)
        val sting = separator.toString()
        assertEquals(6, sting.lengthNoAnsi)
    }

}