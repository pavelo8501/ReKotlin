package po.misc.data.strings

import po.misc.data.styles.Colour


internal fun  stringification(
    receiver: Any?,
    prefix: String = "",
    colour: Colour? = null,
): FormatedEntry{
   return if(receiver != null){
        StringFormatter.formatKnownTypes2(receiver).addPrefix(prefix).applyColour(colour)
    }else{
       FormatedEntry("").addPrefix(prefix).applyColour(colour)
    }
}

fun Any?.stringify(prefix: String, colour: Colour? = null):FormatedEntry = stringification(this, prefix, colour)
fun Any?.stringify(colour: Colour? = null):FormatedEntry = stringification(this, "", colour)







