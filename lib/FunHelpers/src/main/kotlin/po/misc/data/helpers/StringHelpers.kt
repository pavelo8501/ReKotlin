package po.misc.data.helpers

import kotlin.text.StringBuilder
import kotlin.text.replaceFirstChar



@Deprecated("Change to orDefault")
fun Any?.replaceIfNull(replacementText: String = ""): String{
    return this?.let {
        this.toString()
    }?:replacementText
}

@Deprecated("Change to orDefault")
fun <T> T?.replaceIfNull(replacementText: String = "", transform: (T) -> String ): String{
    return if(this != null){
        transform.invoke(this)
    }else{
        replacementText
    }
}

/**
 * Returns the string representation of this object or a replacement text if the value is `null`.
 *
 * This is a convenience extension for nullable `Any` types to safely convert a value to `String`
 * without needing explicit null checks.
 *
 * @param replacementText The text to return if the value is `null`. Defaults to an empty string.
 * @return The result of `toString()` if not null, otherwise `replacementText`.
 */
fun Any?.orDefault(replacementText: String = ""): String{
    return this?.let {
        this.toString()
    }?:replacementText
}


/**
 * Returns a transformed string representation of this object or a replacement text if the value is `null`.
 *
 * This overload allows providing a transformation function to modify non-null values before converting
 * them to a string. This is useful when custom formatting or value extraction is needed.
 *
 * @param replacementText The text to return if the value is `null`. Defaults to an empty string.
 * @param transform A function applied to the non-null value to produce the output string.
 * @return The result of `notNullModification(this)` if not null, otherwise `replacementText`.
 */
fun <T> T?.orDefault(replacementText: String = "", transform: (T) -> String ): String{
    return if(this != null){
        transform.invoke(this)
    }else{
        replacementText
    }
}


fun Char?.orDefault(replacementChar: Char = ' '): Char{
    return this ?: replacementChar
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







