package po.misc.data.pretty_print

import po.misc.data.pretty_print.parts.CommonRowOptions
import po.misc.data.pretty_print.parts.RowOptions


sealed interface RenderableElement<T: Any> {

    val id: Enum<*>?
    val options: RowOptions
    fun renderOnHost(host: T, opts: CommonRowOptions?): String

}
