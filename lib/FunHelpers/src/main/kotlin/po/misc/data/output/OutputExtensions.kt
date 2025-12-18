package po.misc.data.output

import po.misc.context.tracable.TraceableContext
import po.misc.data.strings.ifNotBlank
import po.misc.data.strings.stringify
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.misc.debugging.ClassResolver
import po.misc.debugging.stack_tracer.StackFrameMeta
import po.misc.debugging.toFrameMeta
import po.misc.exceptions.TraceException
import po.misc.exceptions.Tracer
import po.misc.debugging.stack_tracer.ExceptionTrace
import po.misc.debugging.stack_tracer.Methods
import po.misc.debugging.stack_tracer.TraceOptions
import po.misc.exceptions.throwableToText
import po.misc.time.TimeHelper

class OutputHelper<T>(
    val receiver: T,
    val receiverClosure: OutputHelper<T>.(T)-> Unit
): TimeHelper, ClassResolver{
    init {
        receiverClosure.invoke(this, receiver)
    }
}

@PublishedApi
internal fun outputInternal(
    receiver: Any?,
    prefix: String = "",
    colour: Colour? = null
){
    checkDispatcher()
    val effectivePrefix = prefix.ifNotBlank {"$it "}

    when(receiver){
        is List<*>->{
           // receiver.output(prefix = prefix, colour = colour)
            println(effectivePrefix)
            val result =  receiver.stringify(effectivePrefix, colour)
            println(result.formatedText)
        }
        else -> {
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
