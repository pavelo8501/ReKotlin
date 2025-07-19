package po.lognotify.classes.notification

import po.lognotify.classes.notification.models.LogData


interface LoggerContract {
    fun info(message: String): LogData
    fun warn(message: String): LogData
}