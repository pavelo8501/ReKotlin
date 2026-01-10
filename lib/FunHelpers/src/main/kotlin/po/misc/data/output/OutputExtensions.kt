package po.misc.data.output

import po.misc.context.tracable.TraceableContext
import po.misc.data.strings.FormattedPair
import po.misc.data.strings.StringifyOptions
import po.misc.data.strings.stringification
import po.misc.data.strings.stringify
import po.misc.data.styles.Colour
import po.misc.debugging.ClassResolver

interface OutputHelper {
    class OutputParameters(
        val simpleName:String,
    ){
        val size:Int = simpleName.length
        override fun toString(): String = "$simpleName ->"
    }
}

@PublishedApi
internal fun outputInternal(
    receiver: Any?,
    prefix: String = "",
    colour: Colour? = null,
    params: OutputHelper.OutputParameters? = null,
){
    checkDispatcher()

   val usePrefix = if(prefix.isNotBlank()){
        "$prefix -> "
    }else{
        prefix
   }
    val result = receiver.stringify(usePrefix, colour)
    if(params != null){
        if(params.size + result.formatted.length > 140){
            println(params)
            println(result.formatted)
        }else{
            println("$params ${result.formatted}")
        }
    }else{
        println(result.formatted)
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

fun Any?.output(caller: OutputHelper.OutputParameters,  colour: Colour? = null): Unit =
    outputInternal(this, colour = colour,  params =  caller)

fun Any?.output(colour: Colour? = null): Unit = outputInternal(this, colour = colour)
fun Any?.output(prefix: String, colour: Colour? = null): Unit = outputInternal(this, prefix = prefix,  colour)
fun Any?.output(context: TraceableContext, colour: Colour? = null): Unit = outputInternal(context = context, receiver = this, colour =  colour)

internal fun Any?.output(enabled: Boolean, colour: Colour? = null){
    if(enabled){ output(colour = colour) }
    return
}

inline fun <reified T: Any> T.output(prefix: String,  noinline configAction: StringifyOptions.ListOptions.(T) -> Unit){
     val options = StringifyOptions.ListOptions()
     options.header = prefix
     configAction.invoke(options, this)
     val formatted = stringification(this, options)
     println(formatted.toString())
}
