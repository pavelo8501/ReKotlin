package po.misc.data.output

import po.misc.context.tracable.TraceableContext
import po.misc.data.strings.stringify
import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.TextStyler
import po.misc.data.styles.colorize



fun <T: Any> List<T>.output(
    prefix: String = "",
    colour: Colour? = null,
    transform: StringBuilder.(T)-> Any
) {
    checkDispatcher()
    if (prefix.isNotBlank()) {
        if (colour != null) {
            println(TextStyler.ansi.colour(prefix, colour))
        } else {
            println(prefix)
        }
    }
    forEach { entry ->
        val builder = StringBuilder()
        val result = transform.invoke(builder, entry)
        val resultString = builder.toString()
        println(resultString.stringify(colour).styled)
    }
}

fun <T: Any> List<T>.output(
    transform: StringBuilder.(T)-> Any
): Unit = output("", null)

@JvmName("outputTraceableContext")
fun <T: TraceableContext> List<T>.output(provider: OutputProvider = SyncPrint, outputBuilder:T.()-> String){
    checkDispatcher()
    val lines = mutableListOf<String>()
    forEach {
        when(provider){
            is SyncPrint -> {
                val result =  it.outputBuilder()
                println(result)
            }
            is PrintOnComplete -> {
                val result =  it.outputBuilder()
                lines.add(result)
                println(result)
            }
            is LocateOutputs -> OutputDispatcher.locateOutputs()
        }
    }
    if(provider == PrintOnComplete){
        lines.forEach { println(it) }
    }
}

fun <T: Any> List<T>.output(provider: OutputProvider, outputForwarder:(T)-> String){
    checkDispatcher()
    val result =  joinToString {
        outputForwarder.invoke(it)
    }
    println(result)
}