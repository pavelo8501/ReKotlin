package po.misc.data.pretty_print.formatters.text_modifiers

import po.misc.data.pretty_print.cells.PrettyCellBase
import po.misc.data.pretty_print.formatters.FormatterTag
import po.misc.data.pretty_print.formatters.LayoutFormatter
import po.misc.data.pretty_print.parts.cells.RenderRecord
import po.misc.data.pretty_print.parts.rendering.CellRenderNode
import po.misc.data.pretty_print.parts.rendering.RenderParameters
import po.misc.data.strings.EditablePair
import po.misc.data.strings.FormattedPair
import po.misc.data.styles.TextStyler

/**
 * NOTE: Only the first ANSI style segment is preserved.
 * Mixed-style strings are not supported by this formatter.
 */
open class TextTrimmer(
    val trimSubstitution: String
): LayoutFormatter, TextStyler {

    override val dynamic: Boolean = false
    override val tag : FormatterTag = FormatterTag.TextTrimmer

    private val substitutionSize = trimSubstitution.length

    private fun availableTextWidth(trimSize: Int): Int {
       return (trimSize - substitutionSize).coerceAtLeast(0)
    }

    private fun isTrimApplicable(formattedPair: FormattedPair, parameter: RenderParameters): Boolean{
        if(parameter.trimTo != null){
            return true
        }
       return formattedPair.totalPlainLength > parameter.width
    }

    private fun doTrimming(text: String, trimSize: Int): String {
        if(text.length <= trimSize) {
            return text
        }
        val takeSize = availableTextWidth(trimSize)
        return  text.take(takeSize) + trimSubstitution
    }

    private fun trimStyled(formattedPair: EditablePair, width: Int){
        val plainTrimmed = doTrimming(formattedPair.plain, width)
        val segments =  formattedPair.formatted.extractStyleSegments()
        segments.firstOrNull()?.let { segment ->
            val formattedTrimmed = plainTrimmed.applyStyleSegment(segment)
            formattedPair.write(plainTrimmed, formattedTrimmed)
        }
    }
    private fun trimNamed(formattedPair: EditablePair, parameters: RenderParameters) {
         formattedPair.getNamed(PrettyCellBase.KeyValueTags.Value)?.let {namedFormatted->
             val key = formattedPair.getNamed(PrettyCellBase.KeyValueTags.Key)
             val keySize = key?.plainLength ?: 0
             val trimSize =  parameters.width - keySize
             if(namedFormatted.formatted.isStyled){
                 trimStyled(namedFormatted, trimSize)
             }else{
                 val plainTrimmed = doTrimming(namedFormatted.plain, trimSize)
                 namedFormatted.write(plainTrimmed, plainTrimmed)
             }
         }
    }
    private fun trimFormatted(pair: EditablePair, parameters: RenderParameters) {
        if(pair.formatted.isStyled){
            trimStyled(pair, parameters.width)
        }else{
            val plainTrimmed = doTrimming(pair.plain, parameters.width)
            pair.write(plainTrimmed, plainTrimmed)
        }
    }
    override fun modify(pair: EditablePair, parameters: RenderParameters) {
        if(isTrimApplicable(pair, parameters)){
            if(pair.hasNamed){
                trimNamed(pair, parameters)
            }else{
                trimFormatted(pair, parameters)
            }
        }
    }

    override fun modify(record : RenderRecord, parameters: RenderParameters){
        if(isTrimApplicable(record, parameters)){
            val trimSize =  if(parameters.trimTo != null){
               parameters.trimTo?:0
            }else{
                record.totalPlainLength -  (record.plainValueSize - parameters.width)
            }
            val trimmed =  doTrimming(record.plainValue, trimSize)
            val segments =  record.formattedValue.extractStyleSegments()
            val segment = segments.firstOrNull()
            if(segment != null){
                val formattedTrimmed = trimmed.applyStyleSegment(segment)
                record.setValue(trimmed, formattedTrimmed)
            }else{
                record.setValue(trimmed, trimmed)
            }
        }
    }

}
