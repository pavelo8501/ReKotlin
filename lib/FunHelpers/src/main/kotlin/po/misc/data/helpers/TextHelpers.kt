package po.misc.data.helpers

import po.misc.data.styles.SpecialChars


fun String.withIndent(
    count: Int,
    indentionChar: CharSequence = " ",
    prefix: String = "",
    postfix: String = "",
    builder:((String) -> String)? = null
): String{

    val thisText = builder?.invoke(this)?:this
    val indentedText = indentionChar.repeat(count) + thisText

    val result = buildString {
        append(prefix)
        append(indentedText)
        append(postfix)
    }
    return result
}


fun indentText(
    count: Int,
    indentionChar: CharSequence = " ",
    vararg lineBuilder:() -> String
): String{

   val result = lineBuilder.joinToString(separator = SpecialChars.NEW_LINE) { lineProvider->
        lineProvider.invoke().withIndent(count, indentionChar)
    }
   return result
}



fun String.insertTextAfter(
    target: String,
    sourceText: String
): String {
    val index = sourceText.indexOf(target)
    return if (index >= 0) {
        sourceText.take(index + target.length) + this + sourceText.substring(index + target.length)
    } else {
        sourceText
    }
}

fun String.insertTextAfterEach(
    target: String,
    sourceText: String
): String {
    if (target.isEmpty()) return sourceText
    val result = StringBuilder()
    var startIndex = 0
    var index = sourceText.indexOf(target, startIndex)
    while (index >= 0) {
        result.append(sourceText.substring(startIndex, index + target.length))
        result.append(this)
        startIndex = index + target.length
        index = sourceText.indexOf(target, startIndex)
    }
    result.append(sourceText.substring(startIndex))
    return result.toString()
}

