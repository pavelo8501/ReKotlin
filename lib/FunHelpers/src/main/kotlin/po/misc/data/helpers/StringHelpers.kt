package po.misc.data.helpers

import kotlin.text.StringBuilder
import kotlin.text.replaceFirstChar



fun Any?.replaceIfNull(text: String = ""): String{
    return this?.let {
        this.toString()
    }?:text
}

fun <T> T?.replaceIfNull(text: String = "", provider: (T) -> String ): String{
    return if(this != null){
        provider.invoke(this)
    }else{
        text
    }
}



fun String.wrapByDelimiter(
    delimiter: String,
    maxLineLength: Int = 100
): String {
    val parts = this.split(delimiter).map { it.trim() }
    val result = StringBuilder()
    var currentLine = StringBuilder()

    for (part in parts) {
        val candidate = if (currentLine.isEmpty()) part else "${currentLine}$delimiter $part"
        if (candidate.length > maxLineLength) {
            result.appendLine(currentLine.toString().trim())
            currentLine = StringBuilder( "$part $delimiter" )
        } else {
            if (currentLine.isNotEmpty()) currentLine.append(" $delimiter ")
            currentLine.append(part)
        }
    }

    if (currentLine.isNotEmpty()) {
        result.appendLine(currentLine.toString().trim())
    }
    return result.toString()
}


fun String.applyIfNotEmpty(block:String.()-> String): String{
    if(this.isNotEmpty()){
        return this.block()
    }
    return this
}

fun <T: Any> T?.toStringIfNotNull(textIfNull: String? = null , builder:(T)-> String): String{
    return if(this == null){
        textIfNull?:toString()
    }else{
        builder.invoke(this)
    }
}

fun String.stripAfter(char: Char): String = substringBefore(char)


fun <T> Iterable<T>.joinWithIndent(
    count: Int,
    indentChar: CharSequence = " ",
    separator: CharSequence = ", ",
    prefix: CharSequence = "",
    postfix: CharSequence = "",
    limit: Int = -1,
    truncated: CharSequence = "...",
    transform: ((T) -> CharSequence)? = null
): String {
   return joinToString(separator, prefix, postfix, limit, truncated, transform).withIndent(count, indentChar)
}



fun String.firstCharUppercase(): String{
    return replaceFirstChar { it.uppercase() }
}







