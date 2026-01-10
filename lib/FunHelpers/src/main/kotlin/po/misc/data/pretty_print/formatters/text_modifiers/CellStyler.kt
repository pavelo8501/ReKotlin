package po.misc.data.pretty_print.formatters.text_modifiers

import po.misc.data.pretty_print.formatters.FormatterTag
import po.misc.data.pretty_print.formatters.StyleFormatter
import po.misc.data.pretty_print.parts.cells.RenderRecord
import po.misc.data.pretty_print.parts.rendering.StyleParameters
import po.misc.data.strings.EditablePair
import po.misc.data.styles.TextStyler

class CellStyler(): StyleFormatter, TextStyler {

    override val dynamic: Boolean = false
    override val tag: FormatterTag = FormatterTag.TextStyler

    override fun modify(formattedPair: EditablePair, styleParameters: StyleParameters){
        if(formattedPair.namedFormatted.isNotEmpty()) {
            formattedPair.namedFormatted.firstOrNull { it.name.name.lowercase() == "key" }?.let {
                val modified = it.plain.style(styleParameters.keyStyle)
                it.writeFormatted(modified)
            }
            formattedPair.namedFormatted.firstOrNull { it.name.name.lowercase() == "value" }?.let {
                val modified = it.plain.style(styleParameters.style)
                it.writeFormatted(modified)
            }
        }else{
            val modified = formattedPair.plain.style(styleParameters.style)
            formattedPair.writeFormatted(modified)
        }
    }

    override fun modify(record: RenderRecord, styleParameters: StyleParameters){
        if(record.hasKey){
            val styledKey = record.formattedKey.style(styleParameters.keyStyle)
            record.setKey(record.plainKey, styledKey)
        }
        val styledValue = record.formattedValue.style(styleParameters.style)
        record.setValue(record.plainValue, styledValue)
    }

}