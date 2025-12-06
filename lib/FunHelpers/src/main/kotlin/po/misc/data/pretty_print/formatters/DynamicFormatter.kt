package po.misc.data.pretty_print.formatters

import po.misc.data.pretty_print.cells.PrettyCellBase
import po.misc.data.styles.BGColour
import po.misc.data.styles.Colour
import po.misc.data.styles.TextStyler


interface Formatter{
    val formatterName: String
    var formatter: (String, PrettyCellBase) -> String
    fun format(text: String, cell: PrettyCellBase): String
}

class CompositeFormatter(vararg formatter : Formatter){

  private val formatters = mutableMapOf<String, Formatter>()

    init {
        formatter.forEach {
            formatters[it.formatterName] = it
        }
    }

    fun format(text: String, cell: PrettyCellBase): String{
        var result = text
        formatters.values.forEach {
            result = it.format(result, cell)
        }
        return result
    }
}

class DynamicTextFormatter(
    assignedFormatter: ((String, PrettyCellBase) -> String)? = null
): Formatter, TextStyler {

    override val formatterName: String = "DynamicTextFormatter"

    override var formatter: (String, PrettyCellBase) -> String = { text, cell ->
        if(cell.postfix != null){
            "$text ${cell.postfix}"
        }else{
            text
        }
    }

    init {
        assignedFormatter?.let {
            formatter = it
        }
    }
    override fun format(text: String, cell: PrettyCellBase): String{
        return formatter.invoke(text, cell)
    }
}

class DynamicTextStyler(
     assignedFormatter: ((String, PrettyCellBase) -> String)? = null
): Formatter, TextStyler {

    override val formatterName: String = "DynamicTextStyler"

    override var formatter: (String, PrettyCellBase) -> String = { text, cell->
        val useTextStyle = cell.cellOptions.styleOptions.style
        val useColour: Colour? = cell.cellOptions.styleOptions.colour
        val useBackgroundColour: BGColour? =  cell.cellOptions.styleOptions.backgroundColour
        TextStyler.style(text, useTextStyle, useColour, useBackgroundColour)
    }

    init {
        assignedFormatter?.let {
            formatter = it
        }
    }
    override fun format(text: String, cell: PrettyCellBase): String{
        return formatter.invoke(text, cell)
    }
}
