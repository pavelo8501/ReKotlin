package po.misc.data.logging


enum class Verbosity(val minTopic: NotificationTopic){
    Debug(NotificationTopic.Debug),
    Info(NotificationTopic.Info),
    Warnings(NotificationTopic.Warning)
}