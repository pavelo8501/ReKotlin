package po.misc.data.pretty_print.formatters

import po.misc.data.pretty_print.parts.cells.RenderRecord
import po.misc.data.pretty_print.parts.render.CellParameters
import po.misc.data.pretty_print.parts.render.StyleParameters
import po.misc.data.text_span.EditablePair
import po.misc.data.text_span.FormattedText
import po.misc.data.text_span.MutablePair
import po.misc.data.text_span.MutableSpan
import po.misc.types.token.TypeToken


interface FormatterPlugin{
    val tag: FormatterTag
    val dynamic: Boolean
}
interface LayoutFormatter: FormatterPlugin{
    fun modify(mutableSpan: MutableSpan, parameters: CellParameters)
    fun modify(text: String, parameters: CellParameters): String{
        val pair = MutablePair(text)
        modify(pair, parameters)
        return pair.styled
    }
    fun modify(record : RenderRecord, parameters: CellParameters)

}

interface StyleFormatter: FormatterPlugin{
    fun modify(mutableSpan: MutableSpan, styleParameters : StyleParameters)
    fun modify(record: RenderRecord, styleParameters : StyleParameters)

}

interface DynamicStyleFormatter<T>: FormatterPlugin {
    val type: TypeToken<T>
    override val dynamic: Boolean get() = true
    fun modify(mutableSpan: MutableSpan, parameter: T)
    fun modify(text: String, parameter: T): String?{
        val pair = FormattedText(text)
        modify(pair.plain, parameter)
        return pair.styled
    }
}
