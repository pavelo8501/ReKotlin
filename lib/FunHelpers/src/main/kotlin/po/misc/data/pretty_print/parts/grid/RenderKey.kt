package po.misc.data.pretty_print.parts.grid

import po.misc.data.PrettyPrint
import po.misc.data.pretty_print.PrettyGrid
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.PrettyValueGrid
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize



enum class RenderableType(override val formattedString:String) : PrettyPrint{
    Any("Any renderable"),
    Grid(PrettyGrid.prettyName),
    ValueGrid(PrettyValueGrid.prettyName),
    Row(PrettyRow.prettyName),
    ValueRow(PrettyRow.prettyName),
    Placeholder("Placeholder".colorize(Colour.Cyan));
}

class RenderKey(val order: Int, val renderableType: RenderableType){
    internal fun isOfSameTypeChain(): Boolean{
        return renderableType != RenderableType.Placeholder
    }
    fun copy(): RenderKey = RenderKey(order, renderableType)
    override fun equals(other: Any?): Boolean {
        if(other !is RenderKey) return false
        return order == other.order
    }
    override fun hashCode(): Int = order
    override fun toString(): String = "RenderKey<${renderableType}> [Order: $order]"
}