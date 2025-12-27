package po.misc.data.output

import po.misc.context.tracable.TraceableContext
import po.misc.data.ifNotBlank
import po.misc.data.strings.FormattedPair
import po.misc.data.strings.stringify
import po.misc.data.styles.Colour
import po.misc.debugging.ClassResolver
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
            val result =  receiver.stringify(effectivePrefix, colour)
            println(result.formatted)
        }
        is FormattedPair -> {
            println(receiver.formatted)
        }
        else -> {
            val result = receiver.stringify(effectivePrefix, colour)
            println(result.formatted)
        }
    }
}

@PublishedApi
internal fun outputInternal(
    context: TraceableContext,
    receiver: Any?,
    prefix: String = "",
    colour: Colour? = null
) {
    checkDispatcher()
    val info = ClassResolver.instanceInfo(context)
    if (receiver != null) {
        when (receiver) {
            is List<*> -> {
                println(info.formattedString)
                receiver.output(prefix = prefix, colour = colour)
            }
            is FormattedPair -> {
                println(receiver.formatted)
            }
            else -> {
                println(info.formattedString)
                val formattedEntry = receiver.stringify(prefix)
                println(formattedEntry.formatted)
            }
        }
    } else {
        println("${info.formattedString} output null")
    }
}

fun Any?.output(colour: Colour? = null): Unit = outputInternal(this, colour = colour)
fun Any?.output(prefix: String, colour: Colour? = null): Unit = outputInternal(this, prefix = prefix,  colour)
fun Any?.output(context: TraceableContext, colour: Colour? = null): Unit = outputInternal(context = context, receiver = this, colour =  colour)

internal fun Any?.output(enabled: Boolean, colour: Colour? = null): Unit {
    if(enabled){ output(colour = colour) }
    return
}

fun <T: Any> T.output(prefix: String = "", transform: (T)-> Any){
     val result = transform.invoke(this)
     val formatted =  result.stringify(prefix)
     println(formatted.toString())
}
