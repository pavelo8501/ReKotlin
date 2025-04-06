package po.managedtask.exceptions.enums


enum class PropagateType(val value: Int) {
    PROPAGATED(1),
    UNMANAGED(0);

    companion object {
        fun fromValue(value: Int): PropagateType? {
            PropagateType.entries.firstOrNull { it.value == value }?.let {
                return it
            }
            return UNMANAGED
        }
    }
}

enum class CancelType(val value: Int) {
    SKIP_SELF(1),
    CANCEL_ALL(2),
    UNMANAGED(0);

    companion object {
        fun fromValue(value: Int): CancelType? {
            CancelType.entries.firstOrNull { it.value == value }?.let {
                return it
            }
            return UNMANAGED
        }
    }
}

enum class DefaultType(val value: Int) {
    DEFAULT(1),
    GENERIC(2),
    UNMANAGED(0);

    companion object {
        fun fromValue(value: Int): DefaultType? {
            DefaultType.entries.firstOrNull { it.value == value }?.let {
                return it
            }
            return UNMANAGED
        }
    }
}