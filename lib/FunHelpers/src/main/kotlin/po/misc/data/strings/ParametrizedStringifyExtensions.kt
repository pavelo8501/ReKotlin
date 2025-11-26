package po.misc.data.strings

import po.misc.data.styles.Colour
import po.misc.data.styles.colorize


private fun applyIndentOption(
    formatted: FormatedEntry,
    option: IndentOptions,
):FormatedEntry{
    val indentionStr = if(option.indentionColour!= null){
        option.indentionString.colorize(option.indentionColour)
    }else{
        option.indentionString
    }
    formatted.applyColour(option.colour)
    formatted.applyIndention(option.indentionOffset, indentionStr, option.indentionPrefix)
    return formatted
}

fun Any.stringify(
    option: StringifyOptions,
):FormatedEntry {
    val formatted = StringFormatter.formatKnownTypes2(this)
    val complete = when (option) {
        is IndentOptions -> applyIndentOption(formatted, option)
    }
    return complete
}

fun Collection<Any>.stringify(
    option: StringifyOptions,
):FormatedEntry {
    val result = mutableListOf<FormatedEntry>()
    for (entry in this) {

         val formatted = entry.stringify(option)
         result.add(formatted)

//        val formatted = StringFormatter.formatKnownTypes2(entry)
//        val complete = when (option) {
//            is IndentOptions -> applyIndentOption(formatted, option)
//        }
//        result.add(complete)
    }
    return FormatedEntry(result)
}