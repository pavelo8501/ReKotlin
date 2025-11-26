package po.misc.data.pretty_print.formatters

import po.misc.data.pretty_print.cells.PrettyCellBase
import po.misc.data.pretty_print.presets.StylePresets
import po.misc.data.styles.BGColour
import po.misc.data.styles.Colour
import po.misc.data.styles.TextStyler


interface Formatter<P: StylePresets>{
    val formatterName: String
    var formatter: (String, PrettyCellBase<P>) -> String
    fun format(text: String, cell: PrettyCellBase<P>): String
}

class CompositeFormatter<P: StylePresets>(vararg formatter : Formatter<P>){

  private val formatters = mutableMapOf<String, Formatter<P>>()

    init {
        formatter.forEach {
            formatters[it.formatterName] = it
        }
    }

    fun format(text: String, cell: PrettyCellBase<P>): String{
        var result = text
        formatters.values.forEach {
            result = it.format(result, cell)
        }
        return result
    }
}

class DynamicTextFormatter<P: StylePresets>(
    assignedFormatter: ((String, PrettyCellBase<P>) -> String)? = null
): Formatter<P>, TextStyler {

    override val formatterName: String = "DynamicTextFormatter"

    override var formatter: (String, PrettyCellBase<P>) -> String = { text, cell ->
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

    override fun format(text: String, cell: PrettyCellBase<P>): String{
        return formatter.invoke(text, cell)
    }
}

class DynamicTextStyler<P: StylePresets>(
     assignedFormatter: ((String, PrettyCellBase<P>) -> String)? = null
): Formatter<P>, TextStyler {

    override val formatterName: String = "DynamicTextStyler"

    override var formatter: (String, PrettyCellBase<P>) -> String = { text, cell->
        val useTextStyle = cell.options.styleOptions.style
        val useColour: Colour? = cell.options.styleOptions.colour
        val useBackgroundColour: BGColour? =  cell.options.styleOptions.backgroundColour
        TextStyler.style(text, useTextStyle, useColour, useBackgroundColour)
    }

    init {
        assignedFormatter?.let {
            formatter = it
        }
    }

    override fun format(text: String, cell: PrettyCellBase<P>): String{
        return formatter.invoke(text, cell)
    }
}
