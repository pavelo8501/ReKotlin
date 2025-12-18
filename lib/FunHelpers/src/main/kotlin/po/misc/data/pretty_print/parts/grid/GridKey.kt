package po.misc.data.pretty_print.parts.grid

import po.misc.data.pretty_print.PrettyGrid
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.PrettyValueGrid
import kotlin.reflect.KClass


enum class RenderableType (val kClass: KClass<*>) {
    Grid(PrettyGrid::class) ,
    ValueGrid(PrettyValueGrid::class),
    Row(PrettyRow::class),
    ForeignGrid(PrettyGrid::class)

}



class GridKey(
    val order: Int,
    val type: RenderableType
){

    override fun equals(other: Any?): Boolean {
        if(other is GridKey){
           return order == other.order && type == other.type
        }
        return false
    }

    override fun hashCode(): Int {
        var result = order
        result = 31 * result + type.hashCode()
        return result
    }

    override fun toString(): String = "GridKey<${type.name}> [Order: $order]"

}