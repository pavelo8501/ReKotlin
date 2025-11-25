package po.misc.data.strings

import po.misc.data.styles.Colour


@PublishedApi
internal fun stringifyListInternal(
    list: List<Any>,
    colour: Colour?,
):SimpleFormatter {
    return if(list.isNotEmpty()) {
        val stringFormater =  list.first().stringify(prefix = "", colour)
        list.drop(1).forEach {
        }
        SimpleFormatter(stringFormater.text, stringFormater.formatedText)

    }else{
        SimpleFormatter("empty", "empty")
    }
}


@PublishedApi
internal fun buildStringCollection(
    list: Collection<Any?>,
    colour: Colour?,
):FormatedEntry {

   return if(list.isNotEmpty()){
        val rootEntry = list.first().stringify(colour)
        list.drop(1).forEach {
           val result =  it.stringify(colour)
            rootEntry.addFormated(result)
        }
        rootEntry
    }else{
        FormatedEntry("Empty")
    }
}


fun Collection<*>.stringify(
    colour: Colour? = null
):FormatedEntry = buildStringCollection(this, colour = colour)


fun Collection<*>.stringifyList(
    colour: Colour? = null
):FormatedEntry = buildStringCollection(this, colour = colour)


fun Array<*>.stringify(
    colour: Colour? = null
):FormatedEntry = buildStringCollection(this.toList(), colour = colour)

fun Array<*>.stringifyList(
    colour: Colour? = null
):FormatedEntry = buildStringCollection(this.toList(), colour = colour)

