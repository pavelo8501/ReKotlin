package po.misc.data.pretty_print.parts.decorator

import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.parts.render.RenderRole
import po.misc.data.text_span.MutablePair
import po.misc.data.text_span.MutableSpan
import po.misc.data.text_span.TextSpan
import po.misc.data.text_span.SpanBuilder


internal class DecorationPayload(val orientation: Orientation, val value: MutableSpan){
    val plainLength: Int get() =  value.plainLength
    internal val builder = SpanBuilder(value)
    fun prepend(span: TextSpan) {
        builder.prepend(span)
    }
    fun append(span: TextSpan) {
        builder.append(span)
    }
    fun appendPlain(plainText: String) {
        builder.append(plainText)
    }

    fun commitChanges(role: RenderRole): MutablePair{
       return  builder.toMutableSpan(role)
    }
}