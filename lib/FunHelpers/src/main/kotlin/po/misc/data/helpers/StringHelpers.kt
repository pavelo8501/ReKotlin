package po.misc.data.helpers

import po.misc.context.CTX
import po.misc.context.Component
import po.misc.context.TraceableContext
import po.misc.data.PrettyPrint
import po.misc.data.strings.StringFormatter
import po.misc.data.strings.stringify
import po.misc.data.strings.stringifyThis
import po.misc.data.strings.textColorizer
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.misc.exceptions.trackable.TrackableException
import po.misc.exceptions.stack_trace.ExceptionTrace
import po.misc.exceptions.throwableToText
import po.misc.types.helpers.KClassParam
import po.misc.types.helpers.simpleOrNan
import po.misc.types.helpers.toKeyParams
import kotlin.collections.forEach
import kotlin.reflect.KClass
import kotlin.reflect.KTypeParameter
import kotlin.reflect.full.starProjectedType
import kotlin.text.StringBuilder


sealed interface OutputProvider{

}
object SyncPrint :OutputProvider
object PrintOnComplete :OutputProvider

sealed interface DebugProvider{

}

object IdentifyIt :DebugProvider


fun Any.output(){
    println(StringFormatter.formatKnownTypes(this))
}

fun Any.output(transform: (String) -> String){
    stringify(colour = null,  transform).output()
}

fun Any.output(
    colour: Colour
){
    val formated = this.stringify(colour)
    formated.output()
}

fun Any.output(
    prefix: String,
    colour: Colour? = null,
){
    stringify(prefix = prefix, colour = colour).output()
}

fun Any.output(
    prefix: String,
    colour: Colour? = null,
    transform: (String)-> String
){
    val formated = stringify(colour, transform)
    if(prefix.isNotBlank()){
        println( textColorizer(prefix, colour))
        print(formated.toString())
    }else{
        formated.output()
    }
}



fun List<Any>.output(
    prefix: String = "",
    colour: Colour? = null,
    transform: (String)-> String
){
    if(prefix.isNotBlank()){
        println(textColorizer(prefix, colour))
    }
    forEach {
        it.stringify(colour, transform).output()
    }
}

fun List<Any>.output(
    prefix: String = "",
    colour: Colour? = null
){
    if(prefix.isNotBlank()){
        println(textColorizer(prefix, colour))
    }
    forEach {
        it.stringify(colour).output()
    }
}



fun <T: Any> T.output(debugProvider: DebugProvider): KClassParam{
    val thisClass  =  this::class
    val params = thisClass.toKeyParams()
    params.output()
    return params
}

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
    colour: Colour?,
    outputForwarder:((String)-> String)?
) {outputForwarder
    if (target is PrettyPrint) {
       val pretty  = outputForwarder?.invoke(target.formattedString) ?:run {

            target.formattedString
        }
        println(pretty)
    } else {
        val result = when (target) {
            is Enum<*> -> { "${target.name} ${target}"  }
            is CTX -> target.identifiedByName
            else ->   target.toString()
        }

         val lambdaRes  =  outputForwarder?.invoke(result) ?:result

        val colorizedOrNot = colour?.let {
            lambdaRes.colorize(it)
        } ?: run { lambdaRes }
        println(colorizedOrNot)
    }
}


private fun outputKnownClasses(source: Any): Boolean{
    var outputStr = ""
    when(source){
        is KClass<*>->{
            outputStr = buildString {
                appendLine("KClass<${source.simpleOrNan()}>")
            }
        }
        is KTypeParameter->{
            outputStr = buildString {
                appendLine("KTypeParameter[${source.name}]")
                appendLine("Is reified:" + source.isReified)
                appendLine("Upper Bounds:" + source.upperBounds)
                appendLine("StarProjectedType:" + source.starProjectedType)
            }
        }
    }
  return  if(outputStr.isNotBlank()){
        println(outputStr)
        true
    }else{
        false
    }
}


//fun Any.output(colour: Colour? = null, outputForwarder:((String)-> String)? = null) {
//    println("")
//    if(outputKnownClasses(this)){
//        return
//    }
//    outputForwarder?.let {
//       val string = it.invoke(this.toString())
//        outputCreator(string, colour = colour, null)
//    }?:run {
//
//        outputCreator(this, colour = colour, null)
//    }
//}

//fun <T: Component>  T.output(colour: Colour? = null, outputForwarder:((T)-> String)? = null) {
//    outputForwarder?.let {
//        val string = it.invoke(this)
//        outputCreator(string, colour = colour, null)
//    }?:run {
//        outputCreator(this, colour = colour, null)
//    }
//}


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
        is TrackableException->{
           val trace =  exceptionTraceToFormated(exceptionTrace)
           val coroutineString = coroutineInfo?.output()
            buildString {
                appendLine(trace)
                appendLine(coroutineString)
            }
        }
        else -> {
            throwableToText()
        }
    }
    println(text)
}

//fun Array<Any>.output(colour: Colour? = null, outputForwarder:((String)-> String)? = null){
//    iterator().forEach {
//
//        outputCreator(it, colour, outputForwarder)
//    }
//}


//fun <T: Any>  List<T>.output(prefix: String, colour: Colour? = null, outputForwarder:((T)-> String)? = null){
//
//    forEach {
//        outputForwarder?.invoke(it)
//        outputCreator(it, colour, null)
//    }
//}

@JvmName("outputTraceableContext")
fun <T: TraceableContext> List<T>.output(provider: OutputProvider= SyncPrint, outputBuilder:T.()-> String){
    val lines = mutableListOf<String>()
    forEach {
        when(provider){
            is SyncPrint -> {
              val result =  it.outputBuilder()
                outputCreator(result, null, null)
            }
            is PrintOnComplete -> {
                val result =  it.outputBuilder()
                lines.add(result)
                outputCreator(it, null, null)
            }
        }
    }
    if(provider == PrintOnComplete){
        lines.forEach { println(it) }
    }
}

fun <T: Any> List<T>.output(provider: OutputProvider = SyncPrint, outputForwarder:(T)-> String){
    forEach {
       val result = outputForwarder.invoke(it)
        outputCreator(result, null, null)
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







