package po.misc.data.pretty_print.parts.options

import po.misc.data.strings.ElementOptions
import po.misc.data.styles.SpecialChars
import po.misc.data.text_span.FormattedText
import po.misc.data.text_span.TextSpan


data class Margins(
    var leftMargin: Int,
    var spaceFiller: Char = SpecialChars.WHITESPACE_CHAR
){

    fun wrapText(text:String): TextSpan {
        val prefix = spaceFiller.toString().repeat(leftMargin)
        return FormattedText("$prefix$text")
    }

    fun wrapText(TextSpan: FormattedText): TextSpan {
        val prefix = spaceFiller.toString().repeat(leftMargin)
        TextSpan.formattedTextSubEntries.forEach {
            it.plain = "$prefix${it.plain}"
            it.styled = "$prefix${it.styled}"
        }
        TextSpan.plain = "$prefix${TextSpan.plain}"
        TextSpan.styled = "$prefix${TextSpan.styled}"

        val options = ElementOptions(SpecialChars.NEW_LINE)
        if(TextSpan.formattedTextSubEntries.size == 1){
            return TextSpan.joinSubEntries(options)
        }
        if(TextSpan.formattedTextSubEntries.size == 2){
           val formatted = FormattedText(TextSpan.formattedTextSubEntries.first())
           formatted.append(TextSpan.plain, TextSpan.styled)
           formatted.append(TextSpan.formattedTextSubEntries[1])
            return formatted.joinSubEntries(options)
        }
        return  TextSpan.joinSubEntries(options)
    }
}