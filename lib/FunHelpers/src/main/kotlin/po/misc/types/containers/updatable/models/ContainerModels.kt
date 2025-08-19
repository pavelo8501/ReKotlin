package po.misc.types.containers.updatable.models


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
