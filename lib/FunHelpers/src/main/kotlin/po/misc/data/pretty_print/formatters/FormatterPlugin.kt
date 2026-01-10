package po.misc.data.pretty_print.formatters

import po.misc.data.pretty_print.parts.cells.RenderRecord
import po.misc.data.pretty_print.parts.rendering.RenderParameters
import po.misc.data.pretty_print.parts.rendering.StyleParameters
import po.misc.data.strings.EditablePair
import po.misc.data.strings.FormattedText
import po.misc.data.styles.TextStyler
import po.misc.types.token.TypeToken


interface FormatterPlugin{
    val tag: FormatterTag
    val dynamic: Boolean
}
interface LayoutFormatter: FormatterPlugin{
    fun modify(pair: EditablePair, parameters: RenderParameters)
    fun modify(text: String, parameters: RenderParameters): String{
        val pair = FormattedText(text)
        modify(pair, parameters)
        return pair.formatted
    }
    fun modify(record : RenderRecord, parameters: RenderParameters)

}

interface StyleFormatter: FormatterPlugin{
    fun modify(formattedPair: EditablePair, styleParameters : StyleParameters)
    fun modify(record: RenderRecord, styleParameters : StyleParameters)

}

interface DynamicStyleFormatter<T>: FormatterPlugin {
    val type: TypeToken<T>
    override val dynamic: Boolean get() = true
    fun modify(formattedPair: EditablePair, parameter: T)
    fun modify(text: String, parameter: T): String?{
        val pair = FormattedText(text)
        modify(pair.plain, parameter)
        return pair.formatted
    }
}
