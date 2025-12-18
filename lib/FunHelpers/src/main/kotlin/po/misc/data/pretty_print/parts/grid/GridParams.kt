package po.misc.data.pretty_print.parts.grid

import po.misc.data.pretty_print.PrettyGridBase
import po.misc.data.pretty_print.parts.CommonRowOptions
import po.misc.data.pretty_print.parts.RowOptions

data class GridParams(
    val grid: PrettyGridBase<*, *>,
    val usedOptions:  CommonRowOptions?
){

    override fun toString(): String {
       return  "GridParams<${grid.hostType.typeName}, ${grid.type.typeName}>" +
        "Options: $usedOptions"
    }

}