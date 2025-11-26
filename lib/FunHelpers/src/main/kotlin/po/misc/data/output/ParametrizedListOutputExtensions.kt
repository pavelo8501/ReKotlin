package po.misc.data.output

import po.misc.data.strings.IndentOptions
import po.misc.data.strings.ListDirection
import po.misc.data.strings.stringify

fun Collection<Any>.output(
    option: IndentOptions,
    direction: ListDirection = ListDirection.Vertical
){
    val result = stringify(option)
    val joinedString = result.joinFormated(direction)
    println(joinedString)
}