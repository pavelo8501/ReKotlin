package po.misc.data.pretty_print

import po.misc.types.token.TypeToken

interface RenderableElement<T> {
    val typeToken: TypeToken<T>
    fun resolveReceiver(parentReceiver: Any): Collection<T>

}