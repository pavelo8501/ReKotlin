package po.misc.data.output

import po.misc.context.tracable.TraceableContext
import po.misc.data.strings.ifNotBlank
import po.misc.data.strings.stringify
import po.misc.data.styles.Colorizer
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.misc.debugging.ClassResolver
import po.misc.debugging.stack_tracer.StackFrameMeta
import po.misc.debugging.toFrameMeta
import po.misc.exceptions.Tracer
import po.misc.exceptions.stack_trace.ExceptionTrace
import po.misc.exceptions.throwableToText
import po.misc.exceptions.trackable.TrackableException
import po.misc.time.TimeHelper

class OutputHelper<T>(
    val receiver: T,
    val receiverClosure: OutputHelper<T>.(T)-> Unit
): TimeHelper, ClassResolver{

    init {
        receiverClosure.invoke(this, receiver)
    }
}


internal fun checkDispatcher(){

    if(OutputDispatcher.identifyOutput){
        val frame : StackFrameMeta = Tracer().firstTraceElement.toFrameMeta()
        println(frame.consoleLink)
    }
}

fun outputDispatcher(block: OutputDispatcher.()-> Unit){
    block.invoke(OutputDispatcher)
}


@PublishedApi
internal fun outputInternal(
    receiver: Any?,
    prefix: String = "",
    colour: Colour? = null
) {
    checkDispatcher()
    val effectivePrefix = prefix.ifNotBlank {"$it "}
    if (receiver != null) {
         if(receiver is List<*>){
             receiver.output(prefix = prefix, colour = colour)
        }else{
            val result = receiver.stringify(effectivePrefix, colour)
            println(result.formatedText)
        }
    }
}

@PublishedApi
internal fun outputInternal(
    context: TraceableContext,
    receiver: Any?,
    prefix: String = "",
    colour: Colour? = null
){
    checkDispatcher()
    val info = ClassResolver.instanceInfo(context)
    if(receiver != null){
         when(receiver){
            is List<*> -> {
                println(info.formattedString)
                receiver.output(prefix = prefix, colour = colour)
            }
            else ->  {
                println(info.formattedString)
                val formattedEntry = receiver.stringify()
                if(prefix.isNotBlank()){
                    formattedEntry.addPrefix("$prefix ")
                }
                println(formattedEntry.formatedString)
            }
        }
    }else{
        println("${info.formattedString} output null")
    }
}

fun Any?.output(colour: Colour? = null): Unit = outputInternal(this, colour = colour)
fun Any?.output(prefix: String, colour: Colour? = null): Unit = outputInternal(this, prefix = prefix,  colour)

fun Any?.output(context: TraceableContext, colour: Colour? = null): Unit = outputInternal(context = context, receiver = this, colour =  colour)

fun <T: Any> T.output(prefix: String = "", transform: (T)-> Any){
     val result = transform.invoke(this)
     val formatted =  result.stringify()
     println(formatted.addPrefix(prefix).formatedString)
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
