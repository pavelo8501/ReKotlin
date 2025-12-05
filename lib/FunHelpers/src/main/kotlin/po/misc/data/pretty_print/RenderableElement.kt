package po.misc.data.pretty_print

import po.misc.data.pretty_print.parts.CommonRowOptions
import po.misc.types.token.TypeToken

interface RenderableElement<T: Any,  V: Any> {
    val ids: List<Enum<*>>

    fun renderOnHost(host: T, opts: CommonRowOptions?): String
}
