package po.lognotify.exceptions.enums

enum class HandlerType(val value: Int) {

    GENERIC(0),
    SKIP_SELF(1),
    CANCEL_ALL(2),
    UNMANAGED(3);

    companion object {
        fun fromValue(value: Int): HandlerType {
            entries.firstOrNull { it.value == value }?.let {
                return it
            }
            return GENERIC
        }
    }
}