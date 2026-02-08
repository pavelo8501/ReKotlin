package po.misc.data.pretty_print.parts.options

import po.misc.data.helpers.coerceAtLeast
import po.misc.data.helpers.repeat
import po.misc.data.pretty_print.parts.cells.RenderRecord
import po.misc.data.pretty_print.parts.common.ExtendedString
import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.StyleCode
import po.misc.data.styles.TextStyler
import po.misc.data.styles.colorize
import po.misc.data.text_span.EditablePair
import po.misc.data.text_span.TextSpan
import po.misc.data.text_span.prepend
import po.misc.data.text_span.prependCreating

class InnerBorder(
    initialString: String,
    override var styleCode: StyleCode? = null,
    override var enabled:Boolean = true
): ExtendedString, TextStyler{

    constructor(borderChar: Char, colour: Colour? = null, enabled:Boolean = true) : this(borderChar.toString(), colour, enabled)
    var borderString: String =  initialString
        private set

    val isDefault :Boolean get() = borderString == SpecialChars.WHITESPACE

    val borderInfo: String get() = buildString {
        append("Border: '$borderString' ")
        append("Colour: ${styleCode?.name?:"-"} ")
        append("Enabled: $enabled ")
        append("Is Default: $isDefault")
    }

    override var text: String get() = borderString
        set(value) { borderString = value }

    fun applyValues(other: InnerBorder){
        borderString = other.borderString
        styleCode = other.styleCode
        enabled = other.enabled
    }

    fun toStringNoColour(repeat: Int): String {
        return borderString.repeat(repeat)
    }
    fun toString(times: Int):String{
        return styleCode?.let {
            borderString.repeat(times).style(it)
        }?:borderString.repeat(times)
    }

    fun copy(): InnerBorder =  InnerBorder(borderString, styleCode, enabled)

    override fun toString(): String {
        return styleCode?.let {
            borderString.style(it)
        }?:borderString
    }
}

class InnerBorders(
    var leftBorder:  InnerBorder = InnerBorder(" | ", enabled = false)
){
    val bordersEnabled :Boolean get() = leftBorder.enabled
    val displaySize: Int get() = leftBorder.displaySize

    fun disable(){
        leftBorder.enabled = false
    }
    fun wrapText(record: TextSpan):TextSpan {
        if(leftBorder.enabled){
            return  when(record){
                is RenderRecord -> {
                    record.prepend(leftBorder.toString())
                    record
                }
                is TextSpan -> record prependCreating leftBorder.toString()
            }
        }
        return record
    }
}

