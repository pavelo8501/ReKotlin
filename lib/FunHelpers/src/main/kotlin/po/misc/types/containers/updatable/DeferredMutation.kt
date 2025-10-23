package po.misc.types.containers.updatable

import po.misc.callbacks.CallbackManager
import po.misc.context.CTX
import po.misc.types.containers.updatable.models.UpdatableEvents


interface DeferredMutation<T : Any, R : Any>: CTX {
    val receiver: T
    val notifier: CallbackManager<UpdatableEvents>
    fun triggerUpdate(value: R)
    fun triggerUpdate(controlMessage: String)
    //fun compareType(other: Typed<*>): Boolean
}
