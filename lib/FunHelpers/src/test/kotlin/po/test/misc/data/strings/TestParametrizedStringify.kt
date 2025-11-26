package po.test.misc.data.strings

import org.junit.jupiter.api.Test
import po.misc.data.output.output
import po.misc.data.strings.IndentOptions
import po.misc.data.strings.stringify
import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars

class TestParametrizedStringify {

    private fun createStrings(vararg strings: Any): Collection<Any>{
        return strings.toList()
    }

    @Test
    fun `Indention on collection of receivers String`(){
        val strings = createStrings("string_1", "string_2", "string_3")
        val formatted = strings.stringify(IndentOptions(4, "-", indentionPrefix = SpecialChars.WHITESPACE_CHAR))
        formatted.joinFormated(SpecialChars.NEW_LINE).output()
    }

    @Test
    fun `Indention on collection of receivers Int with colour`(){
        val strings = createStrings(1, 2, 3)
        val formatted = strings.stringify(IndentOptions(4, "-", SpecialChars.WHITESPACE_CHAR, indentionColour = Colour.MagentaBright, colour = Colour.Blue))
        formatted.joinFormated(SpecialChars.NEW_LINE).output()
    }

}