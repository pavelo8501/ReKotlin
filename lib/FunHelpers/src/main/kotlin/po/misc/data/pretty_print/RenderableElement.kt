package po.misc.data.pretty_print

import po.misc.types.token.TypeToken

interface RenderableElement<PR: Any,  T> {
    val typeToken: TypeToken<T>
    fun resolveReceiver(parent: PR): Collection<T>
}
