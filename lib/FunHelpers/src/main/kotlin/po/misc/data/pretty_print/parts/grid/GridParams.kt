package po.misc.data.pretty_print.parts.grid

import po.misc.data.pretty_print.PrettyGridBase
import po.misc.data.pretty_print.RenderableElement
import po.misc.data.pretty_print.parts.options.CommonRowOptions


data class GridParams(
    val grid: PrettyGridBase<*>,
    val usedOptions:  CommonRowOptions?,
    val currentMethod:String = ""
){
    var renderableName: String = ""
    override fun toString(): String {
       return  "GridParams"
    }
}