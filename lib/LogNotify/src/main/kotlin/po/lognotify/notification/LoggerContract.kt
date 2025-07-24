package po.lognotify.notification

import po.lognotify.notification.models.TaskData


interface LoggerContract {
    fun info(message: String): TaskData
    fun warn(message: String): TaskData
}