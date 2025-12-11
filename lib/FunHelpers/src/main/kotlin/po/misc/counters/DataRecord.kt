package po.misc.counters


class DataRecord(
    val message: String,
    val type: MessageType,
) {
    enum class MessageType { Info, Success, Failure,  Warning }
    override fun toString(): String = "[${type.name}] $message"
}