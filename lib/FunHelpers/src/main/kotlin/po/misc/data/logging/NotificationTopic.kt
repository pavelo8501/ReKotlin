package po.misc.data.logging


enum class NotificationTopic(val value: Int): Comparable<NotificationTopic>{
    Debug(0),
    Info(1),
    Warning(2),
    Exception(3)
}