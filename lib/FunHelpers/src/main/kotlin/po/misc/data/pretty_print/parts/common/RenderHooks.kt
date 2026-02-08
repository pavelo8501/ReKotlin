package po.misc.data.pretty_print.parts.common

import po.misc.callbacks.signal.signal
import po.misc.callbacks.signal.signalOf
import po.misc.functions.NoResult
import po.misc.types.token.TypeToken

class RenderHooks<T>(
    private val token: TypeToken<T>,
) {
    val onRendered = signalOf(token, NoResult)
}