package po.test.misc.data.output

import org.junit.jupiter.api.Test
import po.misc.data.output.output
import po.misc.data.strings.IndentOptions
import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars

class TestParametrizedListOutput {

    private fun createStrings(vararg strings: Any): Collection<Any>{
        return strings.toList()
    }

    @Test
    fun `Indention on collection of receivers Int with colour`(){
        val strings = createStrings(1, 2, 3)
        strings.output(IndentOptions(4, "-", SpecialChars.WHITESPACE_CHAR, indentionColour = Colour.MagentaBright, colour = Colour.Blue))
    }
}