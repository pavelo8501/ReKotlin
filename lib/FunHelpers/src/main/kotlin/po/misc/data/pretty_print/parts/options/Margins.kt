package po.misc.data.pretty_print.parts.options

import po.misc.data.strings.FormattedPair
import po.misc.data.strings.FormattedText
import po.misc.data.strings.StringifyOptions
import po.misc.data.styles.SpecialChars


data class Margins(
    var leftMargin: Int,
    var spaceFiller: Char = SpecialChars.WHITESPACE_CHAR
){

    fun wrapText(text:String):FormattedPair {
        val prefix = spaceFiller.toString().repeat(leftMargin)
        return  FormattedText("$prefix$text")
    }

    fun wrapText(formattedPair: FormattedText): FormattedPair {
        val prefix = spaceFiller.toString().repeat(leftMargin)
        formattedPair.formattedTextSubEntries.forEach {
            it.plain = "$prefix${it.plain}"
            it.formatted = "$prefix${it.formatted}"
        }

        formattedPair.plain = "$prefix${formattedPair.plain}"
        formattedPair.formatted = "$prefix${formattedPair.formatted}"
        val options = StringifyOptions.ElementOptions(SpecialChars.NEW_LINE)
        if(formattedPair.formattedTextSubEntries.size == 1){
            return formattedPair.joinSubEntries(options)
        }
        if(formattedPair.formattedTextSubEntries.size == 2){
           val formatted = FormattedText(formattedPair.formattedTextSubEntries.first())
           formatted.add(formattedPair.plain, formattedPair.formatted)
           formatted.add(formattedPair.formattedTextSubEntries[1])
            return formatted.joinSubEntries(options)
        }
        return  formattedPair.joinSubEntries(options)
    }
}