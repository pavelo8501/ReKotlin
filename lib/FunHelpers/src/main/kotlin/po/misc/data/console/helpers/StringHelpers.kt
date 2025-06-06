package po.misc.data.console.helpers

import java.util.Spliterator

fun makeIndention(message: String, indentionCount: Int, indentionSymbol: String = " "): String {
    val indent = indentionSymbol.repeat(indentionCount)
    return "$indent $message"
}

fun String.withIndention(indentionCount: Int, indentionSymbol: String = " "): String{
    return makeIndention(this, indentionCount, indentionSymbol)
}

fun String?.emptyOnNull(prefix: String = ""): String{
    if(this != null){
        return "$prefix${this}"
    }
    return ""
}

fun <T> T?.toTemplate(transform: T.() -> String): String =
    this?.let(transform) ?: ""


fun <T> List<T?>.toTemplate(
    separator: String = "\n",
    transform: T.() -> String
): String {
    return this
        .filterNotNull()
        .joinToString(separator = separator) { it.transform() }
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





