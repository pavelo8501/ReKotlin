package po.misc.data.pretty_print.formatters

import po.misc.data.pretty_print.parts.cells.RenderRecord
import po.misc.data.pretty_print.parts.rendering.CellParameters
import po.misc.data.pretty_print.parts.rendering.StyleParameters
import po.misc.data.text_span.EditablePair
import po.misc.types.token.TypeToken
import po.misc.types.token.safeTypedCast



class TextFormatter(
    private val pluginsBacking : MutableList<FormatterPlugin> = mutableListOf()
){
    constructor(vararg formatter: FormatterPlugin): this(formatter.toMutableList())

    val plugins:List<FormatterPlugin> get() =  pluginsBacking
    val hasDynamic: Boolean get() = plugins.any { it.dynamic }
    val size:Int get() = plugins.size

    fun <F:FormatterPlugin> addFormatter(textModifier: F):F{
        pluginsBacking.add(textModifier)
        return textModifier
    }

    fun addFormatters(textModifiers: List<FormatterPlugin>){
        pluginsBacking.addAll(textModifiers)
    }

    fun format(record: RenderRecord, styleParameters : CellParameters){
        plugins.filterIsInstance<LayoutFormatter>().forEach {plugin->
            plugin.modify(record, styleParameters)
        }
    }

    fun format(record: RenderRecord, styleParameters : StyleParameters){
        plugins.filterIsInstance<StyleFormatter>().forEach {plugin->
            plugin.modify(record, styleParameters)
       }
    }

    fun <T> format(TextSpan: EditablePair, parameter:T, typeToken: TypeToken<T>){
        plugins.filterIsInstance<DynamicStyleFormatter<*>>().forEach { plugin ->
            val casted = plugin.safeTypedCast<DynamicStyleFormatter<T>,T>(typeToken)
            casted?.modify(TextSpan, parameter)
        }
    }

    fun getOrNull(tag: FormatterTag): FormatterPlugin? {
        return  plugins.firstOrNull { it.tag == tag }
    }

    operator fun get(tag: FormatterTag): FormatterPlugin {
       return  plugins.first { it.tag == tag }
    }

}