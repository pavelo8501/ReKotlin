package po.misc.callbacks.interfaces

import po.misc.callbacks.CallbackManagerHooks
import po.misc.context.CTX
import po.misc.context.Identifiable

interface ManageableHub<E: Enum<E>> {

    var hooks: CallbackManagerHooks
    val emitter: CTX

}