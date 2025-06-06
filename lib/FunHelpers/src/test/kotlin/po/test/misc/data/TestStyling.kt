package po.test.misc.data

import org.junit.jupiter.api.Test
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.misc.data.styles.text
import po.misc.data.templates.matchTemplate
import po.misc.data.templates.templateRule
import kotlin.test.assertEquals

class TestStyling {

    @Test
    fun `Text colorization`(){

        val unstyled: String = "Simple text"
        val colorized = unstyled.colorize(Colour.CYAN)
        val colorized2 = Colour.BLUE text unstyled

        println(colorized)
        println(colorized2)
    }

    @Test
    fun `Text conditional print`(){

        val text1: String = "Simple text 1"
        val text2: String = "Simple text 2"

        var result : Int = 12

        val resultingText = matchTemplate(
            templateRule(text1){
                result == 10
            },
            templateRule(text2){
                result >= 11
            }
        )

        assertEquals(text2, resultingText, "Wrong template selected")
    }
}