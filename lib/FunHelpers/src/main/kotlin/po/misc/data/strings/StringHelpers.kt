package po.misc.data.strings

import po.misc.data.isUnset
import po.misc.data.styles.SpecialChars



fun <T> Iterable<T>.joinToStringNotBlank(separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", transform: ((T) -> CharSequence)? = null): String {
    val filtered = filter { it.isUnset }
    return filtered.joinTo(StringBuilder(), separator, prefix, postfix, limit, truncated, transform).toString()
}

fun String.prependLines(text: String):String {
    val lines = lines()
    if(lines.size == 1) {
        return "$text${lines[0]}"
    }
    return buildString {
        for(i in 0..<lines.size){
            val line = lines[i]
            if(i < lines.size -1){
                append("$text$line${SpecialChars.NEW_LINE}")
            }else{
                append("$text$line")
            }
        }
    }
}

fun String.appendLines(text: String):String {
    val lines = lines()
    if(lines.size == 1) {
        return "${lines[0]}${text}"
    }
    return buildString {
        for(i in 0..<lines.size){
            val line = lines[i]
            if(i < lines.size -1){
                append("$line$text${SpecialChars.NEW_LINE}")
            }else{
                append("$line$text")
            }
        }
    }
}


