package po.misc.data.strings

import po.misc.data.styles.Colour
import po.misc.data.text_span.FormattedText
import po.misc.data.text_span.MutablePair
import po.misc.data.text_span.TextSpan


@PublishedApi
internal fun buildStringCollection(
    list: Collection<Any?>,
    colour: Colour?,
): TextSpan {

   return if(list.isNotEmpty()){
        val rootEntry = list.first().stringify(colour)
        list.drop(1).forEach {
           val result =  it.stringify(colour)
          //  rootEntry.add(result)
        }
        rootEntry
    }else{
       MutablePair("Empty")
    }
}

fun Collection<*>.stringify(
    colour: Colour? = null
):TextSpan = buildStringCollection(this, colour = colour)


fun Array<*>.stringify(
    colour: Colour? = null
):TextSpan = buildStringCollection(this.toList(), colour = colour)

fun Array<*>.stringifyList(
    colour: Colour? = null
):TextSpan = buildStringCollection(this.toList(), colour = colour)

