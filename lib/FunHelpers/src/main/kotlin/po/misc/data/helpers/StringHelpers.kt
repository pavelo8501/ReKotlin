package po.misc.data.helpers

import po.misc.data.styles.SpecialChars
import po.misc.data.styles.TextStyler
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

fun String.firstCharUppercase(): String{
    return replaceFirstChar { it.uppercase() }
}

fun String.repeat(times: Int, separator: String = SpecialChars.EMPTY): String {
    val result = mutableListOf<String>()
    repeat(times){
        result.add(this)
    }
    return result.joinToString(separator)
}

val String.lengthNoAnsi: Int get() {
    val text = TextStyler.stripAnsi(this)
  return  text.length
}









