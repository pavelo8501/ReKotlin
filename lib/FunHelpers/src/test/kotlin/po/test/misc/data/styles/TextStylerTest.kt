package po.test.misc.data.styles

import po.misc.data.output.output
import po.misc.data.strings.contains
import po.misc.data.styles.Colour
import po.misc.data.styles.TextStyler
import po.misc.data.styles.TextStyler.TextPart
import po.misc.data.styles.colorize
import po.misc.data.text_span.whenRole
import kotlin.test.Test
import kotlin.test.assertTrue

class TextStylerTest : TextStyler {

    private enum class Number { One, Two }

    @Test
    fun `Styling known types`(){
        val result = TextStyler.formatKnown(Number.One)
        result.output()
    }

    @Test
    fun `Styling with modification`(){
       val string = "String_1"
       val result = TextStyler.formatModifying(string){
            it.change(it.plain, it.styled.colorize(Colour.Magenta))
       }
       assertTrue { result.styled.isStyled }
       assertTrue { result.styled.contains(Colour.Magenta) }
    }

    @Test
    fun `Styling key value par with modification`(){
        val result = TextStyler.formatModifying(Number.One){
            it.whenRole(TextPart.Key){
                change(plain, styled.colorize(Colour.Magenta))
            }
            it.whenRole(TextPart.Value){
                change(plain, styled.colorize(Colour.Red))
            }
        }
        assertTrue { result.styled.isStyled }
        assertTrue { result.styled.contains(Colour.Magenta) && result.styled.contains(Colour.Red) }
    }

    @Test
    fun `Styling key value par with partial modification`(){
        val result = TextStyler.formatModifying(Number.Two){
            it.whenRole(TextPart.Key){
                change(plain, styled.colorize(Colour.Green))
            }
        }
        assertTrue { result.styled.isStyled }
        assertTrue { result.styled.contains(Colour.Green) && result.styled.contains(Colour.Cyan) }
    }
}