package po.misc.data.strings

import po.misc.data.styles.Colour


@PublishedApi
internal fun buildStringCollection(
    list: Collection<Any?>,
    colour: Colour?,
): FormattedPair {

   return if(list.isNotEmpty()){
        val rootEntry = list.first().stringify(colour) as FormattedText
        list.drop(1).forEach {
           val result =  it.stringify(colour) as FormattedText
            rootEntry.add(result)
        }
        rootEntry
    }else{
       FormattedText("Empty")
    }
}

fun Collection<*>.stringify(
    colour: Colour? = null
):FormattedPair = buildStringCollection(this, colour = colour)


fun Array<*>.stringify(
    colour: Colour? = null
):FormattedPair = buildStringCollection(this.toList(), colour = colour)

fun Array<*>.stringifyList(
    colour: Colour? = null
):FormattedPair = buildStringCollection(this.toList(), colour = colour)

