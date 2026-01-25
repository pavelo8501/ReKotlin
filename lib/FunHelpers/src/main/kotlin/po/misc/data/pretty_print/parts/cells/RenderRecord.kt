package po.misc.data.pretty_print.parts.cells

import po.misc.data.pretty_print.parts.common.ExtendedString
import po.misc.data.pretty_print.parts.common.Separator
import po.misc.data.styles.TextStyler
import po.misc.data.text_span.MutablePair
import po.misc.data.text_span.TextSpan
import po.misc.data.text_span.prepend


class RenderRecord(
    val value: MutablePair,
    val key : MutablePair?,
    separatorString: ExtendedString?
): TextSpan {

    val separator: Separator = Separator(separatorString)

    override val plain: String get() {
      return  key?.let {
            "${it.plain}${separator}${value.plain}"
        } ?: run {
            value.plain
        }
    }
    override val styled: String get() {
       return key?.let {
            "${it.styled}${separator}${value.styled}"
        } ?: run {
            value.styled
        }
    }
    val hasKey: Boolean = key != null

    fun appendKey(text: String): Unit{
        key?.append(text)
    }
    fun prependKey(text: String): Unit{
        key?.prepend(text)
    }
    fun append(text: String){
        value.append(text)
    }
    fun prepend(text: String){
        if(key != null){
            prependKey(text)
        }else{
            value.prepend(text)
        }
    }
    fun change(plainText: String, styledText:String = plainText){
        if(key != null){
            key.change(plainText, styledText)
        }else{
            value.change(plainText, styledText)
        }
    }
    override fun toString(): String = plain
}