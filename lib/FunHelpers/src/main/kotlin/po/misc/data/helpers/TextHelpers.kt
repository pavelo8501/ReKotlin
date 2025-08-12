package po.misc.data.helpers



fun String.withIndent(
    count: Int, prefix: String,
    postfix: String,
    indentionChar: String = " ",
    builder:((String) -> String)? = null
): String{

    val thisText = builder?.invoke(this)?:this
    val indentedText = indentionChar.repeat(count) + thisText
    val result = buildString {
        appendLine(prefix)
        appendLine(indentedText)
        appendLine(postfix)
    }
    return result
}