package po.misc.data.pretty_print.parts



enum class GridSource { Renderable, Grid }

class GridKey(
    val order: Int,
    val source: GridSource
){

    override fun equals(other: Any?): Boolean {
        if(other is GridKey){
           return order == other.order && source == other.source
        }
        return false
    }

    override fun hashCode(): Int {
        var result = order
        result = 31 * result + source.hashCode()
        return result
    }

}