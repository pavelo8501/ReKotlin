package po.misc.data

import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.misc.data.text_span.FormattedText

class ComparableParameter(
    val name: String,
    val value: Any?,
) {

    var compared: Boolean = false
    var wasEqual: Boolean = false

    private val valueText: String get() {
        if(!compared){
            return value.toString()
        }
        return  if(wasEqual){
            value.toString().colorize(Colour.Green)
        }else{
            value.toString().colorize(Colour.Red)
        }
    }
    val text: String get() =  "${name}: $value"
    val styledText: String get() =  "${name}: $valueText"

    fun compare(other: ComparableParameter) {
        compared = true
        wasEqual = value == other.value
    }
    fun compareBoth(other: ComparableParameter) {
        compared = true
        wasEqual = value == other.value
        other.compared = true
        other.wasEqual = wasEqual
    }

    override fun toString(): String = text
}