package po.misc.data.output

import po.misc.data.strings.IndentOptions
import po.misc.data.strings.ListDirection
import po.misc.data.strings.StringFormatter
import po.misc.data.strings.stringify
import po.misc.data.styles.Colour
import po.misc.debugging.ClassResolver
import po.misc.types.k_class.KClassParam
import po.misc.types.k_class.toKeyParams


fun Any.output(
    option: IndentOptions
){
    val result = stringify(option)
    result.formatedString
    //val joinedString = result.joinFormated(direction)
    println(result.formatedString)
}


fun <T: Any> T.output(debugProvider: DebugProvider): KClassParam{
    val thisClass  =  this::class
    val params = thisClass.toKeyParams()
    params.output()
    return params
}


fun <T> T.output(
    onReceiver: OutputBehaviour,
    prefix: String = ""
): T {
    return if(this != null){
        println(prefix)
        OutputHelper(this){
            when(onReceiver){
                is Identify -> {
                    outputInternal(nowLocalDateTime())
                    val string = ClassResolver.classInfo(it).toString()
                    println(string)
                }
                is Timestamp -> {
                    outputInternal(nowLocalDateTime())
                }
            }
        }
        this
    }else{
        println("$prefix Null")
        this
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

fun Any.output(behaviour: OutputBehaviour){
    OutputHelper(this){receiver->
        when(behaviour){
            is Identify -> {
                outputInternal(nowLocalDateTime())
                val string = ClassResolver.classInfo(receiver).toString()
                println(string)
            }
            is Timestamp -> {
                outputInternal(nowLocalDateTime())
            }
        }
    }
    println(StringFormatter.formatKnownTypes(this))
}


