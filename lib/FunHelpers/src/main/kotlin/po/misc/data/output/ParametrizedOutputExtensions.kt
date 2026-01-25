package po.misc.data.output

import po.misc.data.helpers.orDefault
import po.misc.data.strings.stringify
import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.colorize
import po.misc.debugging.ClassResolver
import po.misc.debugging.models.InstanceInfo
import po.misc.debugging.stack_tracer.TraceOptions
import po.misc.debugging.stack_tracer.trace
import po.misc.types.k_class.KClassParam
import po.misc.types.k_class.toKeyParams


fun <T: Any> T.output(debugProvider: DebugProvider): KClassParam{
    checkDispatcher()
    val thisClass  =  this::class
    val params = thisClass.toKeyParams()
    params.output()
    return params
}

fun output(locate:  LocateOutputs){
    OutputDispatcher.locateOutputs()
}


fun Any.output(
    behaviour: OutputBehaviour,
    prefix: String? = null
){
    checkDispatcher()

    fun stringifyReceiver(receiver: Any):String{
       return when (receiver) {
            is List<*> -> {
                receiver.stringify().styled
            }
            else -> {
                receiver.stringify().styled
            }
        }
    }
    val ownPrefix = "Output -> ".colorize(Colour.Blue)
    val prefixStr = prefix.orDefault { "$it " }
    val receiver = this
    val refactorNotImpl = "Not implemented"
    when(behaviour){
        is ToString -> {
            val receiverStr = receiver.toString()
            val resultStr = "$ownPrefix$prefixStr$receiverStr"
            println(resultStr)
        }
        is Identify -> {
            val info : InstanceInfo =  ClassResolver.resolveInstance(this)
            println(info.formattedString)
            println()
        }
        is Timestamp -> {
          println(refactorNotImpl)
        }
        is HighLight -> {
            val lines = mutableListOf<String>()
            val method = TraceOptions.PreviousMethod
            method.methodName = "output"
            val result = trace(method)
            val str = stringifyReceiver(this)
            val console = result.bestPick.consoleLink
            lines.add("")
            if(prefix != null){
                lines.add(prefix)
            }
            lines.add("Highlight output -> ".colorize(Colour.Blue))
            lines.add(console)
            lines.add(str)
            lines.add("End of output".colorize(Colour.Blue))
            lines.add("")
            val report =  lines.joinToString(SpecialChars.NEW_LINE)
            println(report)
        }
    }
}

fun <T: Any> T.output(pass:Pass, colour: Colour? = null): T {
    outputInternal(this, colour = colour)
    return this
}
fun <T: Any, R> T.output(pass:Pass,  colour: Colour? = null, selector: T.() ->R): R {
    outputInternal(this, colour = colour)
    return selector(this)
}
