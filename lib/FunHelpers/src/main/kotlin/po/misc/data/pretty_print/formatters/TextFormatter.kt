package po.misc.data.pretty_print.formatters

import po.misc.collections.asList
import po.misc.data.pretty_print.formatters.text_modifiers.CellStyler
import po.misc.data.pretty_print.formatters.text_modifiers.ConditionalTextModifier
import po.misc.data.pretty_print.formatters.text_modifiers.Formatter
import po.misc.data.pretty_print.formatters.text_modifiers.TextModifier
import po.misc.data.pretty_print.parts.Style
import po.misc.types.castOrThrow
import po.misc.types.token.TypeToken


class TextFormatter(
    vararg formatter: TextModifier
) {

    private val formattersBacking = mutableListOf(*formatter)

    val formatters:List<TextModifier> get() =  formattersBacking.sortedByDescending { it.priority }

    @PublishedApi
    internal val conditionalFormatters:List<ConditionalTextModifier<*>> get() =
        formattersBacking.filterIsInstance<ConditionalTextModifier<*>>()


    fun <F:TextModifier> addFormatter(textModifier: F):F{
        formattersBacking.add(textModifier)
        return textModifier
    }

    fun style(text: String, styleOption: Style? = null): String {
//        val modifier = formatters.firstOrNull { it.formatter == Formatter.ColorModifier }
//        if (modifier != null) {
//            val modified = modifier.modify(text)
//            return modified
//        }
        val styler = formatters.firstOrNull { it.formatter == Formatter.TextStyler }
        if(styleOption != null && styler is CellStyler) {
            val styled =   styler.modify(text, styleOption)
            return styled
        }
        if (styler != null) {
            val styled = styler.modify(text)
            return styled
        }
        return text
    }

    fun <T> conditionalStyle(text: String, parameter:T, typeToken: TypeToken<T>): String? {
        val filtered =  conditionalFormatters.filter { it.type == typeToken }
        var result :String? = null
        for(formatter in filtered){
            val casted = formatter.castOrThrow<ConditionalTextModifier<T>>()
            val modified = casted.modify(text, parameter)
            if (modified != null){
                result = modified
            }
        }
        return result
    }

    inline fun <reified T> conditionalStyle(text: String, parameter:T): String?  =
        conditionalStyle(text, parameter, TypeToken<T>())

    operator fun get(formatter: Formatter): TextModifier? {
       return  formatters.firstOrNull { it.formatter == formatter }
    }

}