package po.misc.data.helpers

import po.misc.context.CTX
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import kotlin.text.StringBuilder


fun Any?.replaceIfNull(text: String = ""): String{
    return this?.let {
        this.toString()
    }?:text
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

fun Any.output(colour: Colour? = null){
    val classString = when(this){
        is CTX->identifiedByName
        else -> this.toString()
    }
    if(colour != null){
        println(classString.colorize(colour))
    }else{
        println(classString)
    }
}

fun Array<Any>.output(colour: Colour? = null){
    iterator().forEach {
        it.output(colour)
    }
}

fun List<Any>.output(colour: Colour? = null){
    forEach {
        it.output(colour)
    }
}

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







