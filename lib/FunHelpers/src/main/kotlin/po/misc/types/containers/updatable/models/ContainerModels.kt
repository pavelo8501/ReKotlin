package po.misc.types.containers.updatable.models

import po.misc.interfaces.IdentifiableContext



enum class UpdatableEvents{
    OnArmed,
    UpdateInvoked,
    Failure
}

data class UpdatableData(
    val event:UpdatableEvents,
    val receiver: Any,
    val message: String,
    val ok: Boolean,
)
