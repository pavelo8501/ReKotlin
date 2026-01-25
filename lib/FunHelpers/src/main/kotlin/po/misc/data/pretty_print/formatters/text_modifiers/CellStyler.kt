package po.misc.data.pretty_print.formatters.text_modifiers

import po.misc.data.pretty_print.formatters.FormatterTag
import po.misc.data.pretty_print.formatters.StyleFormatter
import po.misc.data.pretty_print.parts.cells.RenderRecord
import po.misc.data.pretty_print.parts.rendering.StyleParameters
import po.misc.data.styles.TextStyler
import po.misc.data.text_span.EditablePair

class CellStyler(): StyleFormatter, TextStyler {

    override val dynamic: Boolean = false
    override val tag: FormatterTag = FormatterTag.TextStyler

    override fun modify(TextSpan: EditablePair, styleParameters: StyleParameters){
        if(TextSpan.namedFormatted.isNotEmpty()) {
            TextSpan.namedFormatted.firstOrNull { it.name.name.lowercase() == "key" }?.let {
                val modified = it.plain.style(styleParameters.keyStyle)
                it.writeFormatted(modified)
            }
            TextSpan.namedFormatted.firstOrNull { it.name.name.lowercase() == "value" }?.let {
                val modified = it.plain.style(styleParameters.style)
                it.writeFormatted(modified)
            }
        }else{
            val modified = TextSpan.plain.style(styleParameters.style)
            TextSpan.writeFormatted(modified)
        }
    }

    override fun modify(record: RenderRecord, styleParameters: StyleParameters){
        record.key?.let {
            val styledKey = it.plain.style(styleParameters.keyStyle)
            it.change(it.plain, styledKey)
            val styledValue = record.value.plain.style(styleParameters.style)
            record.value.change(record.value.plain, styledValue)
        }?:run {
            val styledValue = record.plain.style(styleParameters.style)
            record.change(record.plain, styledValue)
        }
    }

}