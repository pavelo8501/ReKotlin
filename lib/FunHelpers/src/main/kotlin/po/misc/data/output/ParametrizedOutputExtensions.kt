package po.misc.data.output

import po.misc.data.PrettyFormatted
import po.misc.data.helpers.orDefault
import po.misc.data.strings.IndentOptions
import po.misc.data.strings.ListDirection
import po.misc.data.strings.StringFormatter
import po.misc.data.strings.stringify
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.misc.debugging.ClassResolver
import po.misc.debugging.stack_tracer.StackFrameMeta
import po.misc.types.k_class.KClassParam
import po.misc.types.k_class.toKeyParams


fun Any.output(
    option: IndentOptions
){
    checkDispatcher()
    val result = stringify(option)
    result.formatedString
    //val joinedString = result.joinFormated(direction)
    println(result.formatedString)
}


fun <T: Any> T.output(debugProvider: DebugProvider): KClassParam{
    checkDispatcher()
    val thisClass  =  this::class
    val params = thisClass.toKeyParams()
    params.output()
    return params
}


fun Any.output(
    behaviour: OutputBehaviour,
    prefix: String? = null
){
    checkDispatcher()
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
            println(refactorNotImpl)
        }
        is Timestamp -> {
          println(refactorNotImpl)
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

fun PrettyFormatted.output(vararg section: Enum<*>){
    checkDispatcher()
    val formated =  if(section.isNotEmpty()){
        formatted(section.toList())
    }else{
        formatted()
    }
    println(formated)
}
