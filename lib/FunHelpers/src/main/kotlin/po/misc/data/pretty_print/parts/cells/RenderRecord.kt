package po.misc.data.pretty_print.parts.cells

import po.misc.data.TextWrapper
import po.misc.data.pretty_print.parts.common.ExtendedString
import po.misc.data.pretty_print.parts.common.Separator
import po.misc.data.text_span.MutablePair
import po.misc.data.text_span.MutableSpan
import po.misc.data.text_span.MutableSpanBase
import po.misc.data.text_span.SpanRole
import po.misc.data.text_span.TextSpan
import po.misc.data.text_span.asMutable


class RenderRecord(
    initialValue: TextSpan,
    initialKey : MutablePair?,
    separatorString: ExtendedString?
): MutableSpanBase(){

    val value: MutablePair = initialValue.asMutable()
    val key : MutablePair? = initialKey?.asMutable()

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
    override fun prepend(other: TextSpan) {
        if(key != null){
            key.prepend(other)
        }else{
            value.prepend(other)
        }
    }
    override fun append(other: TextSpan) {
        value.append(other)
    }

    override fun change(other: TextSpan): RenderRecord{
        if(key != null){
            key.change(other)
        }else{
            value.change(other)
        }
        return this
    }
    override fun copy(newRole: SpanRole?): RenderRecord {
        return RenderRecord(value, key, separator)
    }
    override fun toString(): String = plain
}