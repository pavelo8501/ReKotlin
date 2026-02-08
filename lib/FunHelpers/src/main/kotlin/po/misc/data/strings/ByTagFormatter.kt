package po.misc.data.strings

import po.misc.data.text_span.MutablePair
import po.misc.data.text_span.MutableSpan
import po.misc.data.text_span.StyledPair
import po.misc.data.text_span.TextSpan

class StyleRegistry {

    val stylers: MutableMap<Enum<*>, (String) -> String> = mutableMapOf()

    fun addStyler(styleTag: Enum<*>, formatter:  (String) -> String){
        stylers[styleTag] = formatter
    }

    fun styleAsString(styleTag: Enum<*>, text:String): String{
       return stylers[styleTag]?.invoke(text)?:text
    }
    fun styleAsPair(styleTag: Enum<*>, text:String): MutablePair{
       return stylers[styleTag]?.let {
           MutablePair(text, it.invoke(text))
        }?: MutablePair(text)
    }


}