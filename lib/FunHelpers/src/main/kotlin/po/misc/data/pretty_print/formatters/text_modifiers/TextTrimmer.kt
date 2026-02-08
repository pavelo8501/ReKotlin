package po.misc.data.pretty_print.formatters.text_modifiers

import po.misc.data.pretty_print.formatters.FormatterTag
import po.misc.data.pretty_print.formatters.LayoutFormatter
import po.misc.data.pretty_print.parts.cells.RenderRecord
import po.misc.data.pretty_print.parts.render.CellParameters
import po.misc.data.styles.TextStyler
import po.misc.data.text_span.MutableSpan

/**
 * NOTE: Only the first ANSI style segment is preserved.
 * Mixed-style strings are not supported by this formatter.
 */
open class TextTrimmer(
    val trimSubstitution: String
): LayoutFormatter, TextStyler {

    override val dynamic: Boolean = false
    override val tag: FormatterTag = FormatterTag.TextTrimmer

    private val substitutionSize = trimSubstitution.length

    private fun availableTextWidth(trimSize: Int): Int {
        return (trimSize - substitutionSize).coerceAtLeast(0)
    }

    private fun doTrimming(text: String, trimSize: Int): String {
        if (text.length <= trimSize) {
            return text
        }
        val takeSize = availableTextWidth(trimSize)
        return text.take(takeSize) + trimSubstitution
    }

//    private fun trimStyled(textSpan: EditablePair, width: Int) {
//        val plainTrimmed = doTrimming(textSpan.plain, width)
//        val segments = textSpan.styled.extractStyleSegments()
//        segments.firstOrNull()?.let { segment ->
//            val formattedTrimmed = plainTrimmed.applyStyleSegment(segment)
//            textSpan.write(plainTrimmed, formattedTrimmed)
//        }
//    }

//    private fun trimNamed(textSpan: EditablePair, parameters: RenderParameters) {
//        textSpan.getNamed(PrettyCellBase.KeyValueTags.Value)?.let { namedFormatted ->
//            val key = textSpan.getNamed(PrettyCellBase.KeyValueTags.Key)
//            val keySize = key?.plainLength ?: 0
//
//            val trimSize = textSpan.plainLength - keySize
//            if (namedFormatted.styled.isStyled) {
//                trimStyled(namedFormatted, trimSize)
//            } else {
//                val plainTrimmed = doTrimming(namedFormatted.plain, trimSize)
//                namedFormatted.write(plainTrimmed, plainTrimmed)
//            }
//        }
//    }

    override fun modify(mutableSpan: MutableSpan, parameters: CellParameters) {

    }

    fun trimKeyless(record: RenderRecord, parameters: CellParameters) {
        val diff = parameters.maxWidth -  record.plainLength
        val trimmed = doTrimming(record.plain, diff)
        val segments = record.styled.extractStyleSegments()
        val segment = segments.firstOrNull()
        if (segment != null) {
            val formattedTrimmed = trimmed.applyStyleSegment(segment)
            record.change(trimmed, formattedTrimmed)
        } else {
            record.change(trimmed)
        }
    }

    override fun modify(record: RenderRecord, parameters: CellParameters) {
        if (parameters.maxWidth == 0 || parameters.maxWidth > record.plainLength ) {
            return
        }
        if(!record.hasKey){
            return trimKeyless(record, parameters)
        }else{
            record.key?.let {
                val diff =  (parameters.maxWidth - it.plainLength) - record.plainLength
                val trimmed = doTrimming(it.plain, diff)
                val segments = it.styled.extractStyleSegments()
                val segment = segments.firstOrNull()
                if (segment != null) {
                    val formattedTrimmed = trimmed.applyStyleSegment(segment)
                    it.change(trimmed, formattedTrimmed)
                } else {
                    it.change(trimmed)
                }
            }
        }
    }
}
