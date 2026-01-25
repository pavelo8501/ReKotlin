package po.misc.data.pretty_print.formatters

import po.misc.data.pretty_print.parts.cells.RenderRecord
import po.misc.data.pretty_print.parts.rendering.CellParameters
import po.misc.data.pretty_print.parts.rendering.StyleParameters
import po.misc.data.text_span.EditablePair
import po.misc.data.text_span.FormattedText
import po.misc.types.token.TypeToken


interface FormatterPlugin{
    val tag: FormatterTag
    val dynamic: Boolean
}
interface LayoutFormatter: FormatterPlugin{
    fun modify(pair: EditablePair, parameters: CellParameters)
    fun modify(text: String, parameters: CellParameters): String{
        val pair = FormattedText(text)
        modify(pair, parameters)
        return pair.styled
    }
    fun modify(record : RenderRecord, parameters: CellParameters)

}

interface StyleFormatter: FormatterPlugin{
    fun modify(TextSpan: EditablePair, styleParameters : StyleParameters)
    fun modify(record: RenderRecord, styleParameters : StyleParameters)

}

interface DynamicStyleFormatter<T>: FormatterPlugin {
    val type: TypeToken<T>
    override val dynamic: Boolean get() = true
    fun modify(TextSpan: EditablePair, parameter: T)
    fun modify(text: String, parameter: T): String?{
        val pair = FormattedText(text)
        modify(pair.plain, parameter)
        return pair.styled
    }
}
