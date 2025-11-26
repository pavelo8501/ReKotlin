package po.misc.data.pretty_print

import po.misc.types.token.TypeToken

interface RenderableElement<T> {
    val typeToken: TypeToken<T>
   // val transition: (Any)-> T

    fun resolveReceiver(parentReceiver: Any):T

}