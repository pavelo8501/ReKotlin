package po.misc.data.strings

import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars


sealed interface StringifyOptions

data class IndentOptions(
    val indentionOffset: Int,
    val indentionString: String,
    val indentionPrefix: Char? = null,
    val indentionColour: Colour? = null,
    val colour: Colour? = null
) : StringifyOptions



sealed interface ListDirection{
    object Vertical: ListDirection
    object Horizontal:ListDirection{
        var separator: String = SpecialChars.COMA

        fun changeSeparator(separationString: String):Horizontal{
            separator = separationString
            return this
        }
    }
}




