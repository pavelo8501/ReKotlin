package po.misc.data.output

import po.misc.data.helpers.orDefault
import po.misc.data.strings.IndentOptions
import po.misc.data.strings.ListDirection
import po.misc.data.strings.stringify
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize

fun Collection<Any>.output(
    option: IndentOptions,
    direction: ListDirection = ListDirection.Vertical
){
    checkDispatcher()
    val result = stringify(option)
    val joinedString = result.joinFormated(direction)
    println(joinedString)
}


fun Collection<Any>.output(
    behaviour: OutputBehaviour,
    prefix: String? = null
){
    checkDispatcher()
    val ownPrefix = "Output -> ".colorize(Colour.Blue)
    println(ownPrefix)
    if(prefix != null){
        println(prefix)
    }
    val refactorNotImpl = "Not implemented"
    when(behaviour){
        is ToString -> {
            forEach {
                println(it.toString())
            }
        }
        is Identify -> {
            println(refactorNotImpl)
        }
        is Timestamp -> {
            println(refactorNotImpl)
        }
    }
}