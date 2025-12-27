package po.misc.data.pretty_print.parts.grid

import po.misc.data.pretty_print.PrettyGridBase
import po.misc.data.pretty_print.RenderableElement
import po.misc.data.pretty_print.parts.options.CommonRowOptions



data class GridParams(
    val grid: PrettyGridBase<*, *>,
    val usedOptions:  CommonRowOptions?,
    val currentMethod:String = ""
){
    constructor(
        grid: PrettyGridBase<*, *>,
        currentMethod:String = "",
        renderable: RenderableElement<*, *>
    ): this(grid, null, currentMethod){
        renderableName = renderable.toString()
    }
    var renderableName: String = ""
    override fun toString(): String {
       return  "GridParams<${grid.hostType.typeName}, ${grid.valueType.typeName}>" +
        "Options: $usedOptions"
    }

}