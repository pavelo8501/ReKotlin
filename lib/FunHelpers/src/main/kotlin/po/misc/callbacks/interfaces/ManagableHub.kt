package po.misc.callbacks.interfaces

import po.misc.callbacks.CallbackManagerHooks
import po.misc.interfaces.IdentifiableContext

interface ManageableHub<E: Enum<E>> {

    var hooks: CallbackManagerHooks
    val emitter: IdentifiableContext

}