package po.misc.data.helpers

import po.misc.context.CTX
import po.misc.data.PrettyPrint
import po.misc.data.printable.PrintableBase
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.misc.exceptions.TrackableException
import po.misc.exceptions.TrackableScopedException
import po.misc.exceptions.models.ExceptionTrace
import po.misc.exceptions.throwableToText
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

private fun outputCreator(
    target: Any,
    prefixText: String,
    colour: Colour?,
    outputForwarder:((String)-> Unit)?
): String{
    var isPrettyPrint: Boolean = false
    val classString = when(target){
        is PrettyPrint-> {
            isPrettyPrint = true
            target.formattedString
        }
        is Enum<*>-> target.name
        is CTX-> target.identifiedByName
        else -> target.toString()
    }

    val outputString = if(prefixText.isBlank()){
        classString
    }else{
        "$prefixText $classString"
    }

    return  if(colour != null && !isPrettyPrint){
        val colorized = outputString.colorize(colour)
        outputForwarder?.invoke(colorized)?.toString()?: colorized
    }else{
        outputForwarder?.invoke(outputString)?.toString()?:outputString
    }

}

fun Any.output(colour: Colour? = null, outputForwarder:((String)-> Unit)? = null){
   val output = outputCreator(this, "",  colour, outputForwarder)
   println(output)
}

fun Throwable.output(){

    fun exceptionTraceToFormated(exceptionTrace : ExceptionTrace): String{
      return  exceptionTrace.bestPick.let {
            buildString {
                appendLine(throwableToText().colorize(Colour.RedBright))
                appendLine("Thrown in".colorize(Colour.YellowBright))
                appendLine("ClassName: ${it.simpleClassName}".colorize(Colour.YellowBright))
                appendLine("Method Name: ${it.methodName}".colorize(Colour.YellowBright))
                appendLine("Line nr: ${it.lineNumber}".colorize(Colour.YellowBright))
            }
        }
    }


    val text =  when(this){
        is TrackableScopedException->{
           val trace =  exceptionTraceToFormated(exceptionTrace)
           val coroutineString = coroutineInfo?.output()
            buildString {
                appendLine(trace)
                appendLine(coroutineString)
            }
        }
        is TrackableException->{
            exceptionTraceToFormated(exceptionTrace)
        }
        else -> {
            throwableToText()
        }
    }
    println(text)
}


fun Any.output(
    prefix: String,
    colour: Colour? = null,
    outputForwarder:((String)-> Unit)? = null
){
    outputCreator(this, prefix, colour, outputForwarder)
}

fun Array<Any>.output(colour: Colour? = null, outputForwarder:((String)-> Unit)? = null){
    iterator().forEach {
        outputCreator(it, "", colour, outputForwarder)
    }
}

fun List<Any>.output(prefix: String, colour: Colour? = null, outputForwarder:((String)-> Unit)? = null){
    forEach {
        outputCreator(it, prefix, colour, outputForwarder)
    }
}

fun List<Any>.output(colour: Colour? = null, outputForwarder:((String)-> Unit)? = null){
    forEach {
        outputCreator(it,  "", colour, outputForwarder)
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







