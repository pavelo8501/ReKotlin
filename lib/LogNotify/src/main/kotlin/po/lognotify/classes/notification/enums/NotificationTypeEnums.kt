package po.lognotify.classes.notification.enums


enum class InfoProvider{
    TASK,
    EX_THROWER,
    EX_HANDLER
}

enum class EventType(val eventId: Int){
    UNKNOWN(0),
    START(1),
    STOP(2),
    SYSTEM_MESSAGE(2),
    HANDLER_REGISTERED(3),
    EXCEPTION_HANDLED(4),
    EXCEPTION_UNHANDLED(5),
    EXCEPTION_THROWN(6);

    val emojis : Map<Int, String> =  mapOf(1 to "Start", 2 to "Stop", 3 to "System", 4 to "✔", 5 to "", 6 to "⚠", 7 to "😈")

    companion object {
        fun fromValue(eventId: Int): EventType? {
            entries.firstOrNull { it.eventId == eventId }?.let {
                return it
            }
            return UNKNOWN
        }
    }
}