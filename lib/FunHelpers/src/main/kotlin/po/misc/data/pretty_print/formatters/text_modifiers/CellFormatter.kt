package po.misc.data.pretty_print.formatters.text_modifiers

import po.misc.data.pretty_print.cells.PrettyCellBase

class CellFormatter(
   val cell:  PrettyCellBase
) :TextModifier{

    override val formatter : Formatter = Formatter.CellFormatter

    fun modify(cell: PrettyCellBase,  text: String): String {
        val postfix = cell.postfix
         return  if(postfix != null){
            "$text$postfix"
        }else{
            text
        }
    }

    override fun modify(text: String): String {
        return modify(cell, text)
    }

}