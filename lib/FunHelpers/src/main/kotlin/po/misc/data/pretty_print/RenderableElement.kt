package po.misc.data.pretty_print

import po.misc.data.pretty_print.parts.CommonRowOptions
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.types.token.TypeToken


interface RenderableElement<T: Any> {
    //val ids: List<Enum<*>>
    val id: Enum<*>?
    val options: RowOptions
    fun renderOnHost(host: T, opts: CommonRowOptions?): String
}
