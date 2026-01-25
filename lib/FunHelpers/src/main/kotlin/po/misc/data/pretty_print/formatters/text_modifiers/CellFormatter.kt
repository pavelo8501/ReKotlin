package po.misc.data.pretty_print.formatters.text_modifiers

import po.misc.data.pretty_print.cells.PrettyCellBase
import po.misc.data.pretty_print.formatters.FormatterTag
import po.misc.data.pretty_print.formatters.LayoutFormatter
import po.misc.data.pretty_print.parts.cells.RenderRecord
import po.misc.data.pretty_print.parts.rendering.CellParameters
import po.misc.data.text_span.EditablePair


class CellFormatter() : LayoutFormatter {

    override val dynamic: Boolean = false
    override val tag : FormatterTag = FormatterTag.CellFormatter

    fun modify(cell: PrettyCellBase<*>,  text: String): String {
        val postfix = cell.postfix
         return  if(postfix != null){
            "$text$postfix"
        }else{
            text
        }
    }

    override fun modify(pair: EditablePair, parameters: CellParameters) {
        TODO("Not yet implemented")
    }

    override fun modify(record: RenderRecord, parameters: CellParameters){

        TODO("Not yet implemented")
    }

}