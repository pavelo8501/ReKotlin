package po.misc.types.containers.updatable

import po.misc.callbacks.CallbackManager
import po.misc.interfaces.ClassIdentity
import po.misc.interfaces.IdentifiableClass
import po.misc.types.Typed
import po.misc.types.containers.updatable.models.UpdatableEvents


interface DeferredMutation<T : Any, R : Any>: IdentifiableClass {
    val receiver: T
    override val identity: ClassIdentity
    val notifier: CallbackManager<UpdatableEvents>
    fun triggerUpdate(value: R)
    fun triggerUpdate(controlMessage: String)
    fun compareType(other: Typed<*>): Boolean


}
