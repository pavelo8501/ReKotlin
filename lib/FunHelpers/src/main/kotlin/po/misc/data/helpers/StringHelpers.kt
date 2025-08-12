package po.misc.data.helpers

import po.misc.data.styles.SpecialChars

fun makeIndention(message: String, indentionCount: Int, indentionSymbol: String = " "): String {
    val indent = indentionSymbol.repeat(indentionCount)
    return "$indent $message"
}

fun String.withIndention(indentionCount: Int, indentionSymbol: String = " "): String{
    return makeIndention(this, indentionCount, indentionSymbol)
}

private fun makeMargins(text: String, topMargin: Int, bottomMargin: Int): String{
    var result = ""
    for(i in 1..topMargin){
        result+= SpecialChars.NewLine
    }
    result += text
    for(i in 1..bottomMargin){
        result+= SpecialChars.NewLine
    }
   return result
}

fun String.withMargin(topMargin: Int, bottomMargin: Int): String{
    return makeMargins(this, topMargin, bottomMargin)
}

fun String.withMargin(vMargin: Int): String{
    return makeMargins(this, vMargin, vMargin)
}


fun String?.emptyOnNull(alternativeText: String = ""): String{
    if(this != null){
        return "$alternativeText${this}"
    }
    return ""
}

fun String.ifNotEmpty(string: String):String{
    return if (this.isNotEmpty()){
        string
    }else{
        ""
    }
}

fun String?.emptyAsNull(): String?{
    if(this != null && this.count() > 0){ return this }
    return null
}

fun String?.emptyIfNullOrText(textProvider:(String)->String): String{
    return if(this == null){
        ""
    }else{
        textProvider.invoke(this)
    }
}


fun <T: Any>  Any?.textIfNotNull(textOnNull: String = "", sourceProvider: T.()-> String): String{
    return this?.let {
        @Suppress("UNCHECKED_CAST")
        sourceProvider.invoke(it as T)
    }?:textOnNull
}

fun Any?.textIfNull(fallbackText: String, textProvider: (Any)-> String): String{
   return this?.let {
        textProvider.invoke(it)
    }?:fallbackText
}


fun Any?.textIfNull(text: String): String{
    return this?.let {
        this.toString()
    }?:text
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


fun String.applyIfNotEmpty(block:String.()-> String): String{
    if(this.isNotEmpty()){
        return this.block()
    }
    return this
}

fun String?.applyIfNull(block:String.()-> String): String{
    return this?.block()?:""
}

fun String.stripAfter(char: Char): String = substringBefore(char)

fun String.output(){
    println(this)
}

fun Array<String>.output(){
    iterator().forEach {
        println(it)
    }
}







