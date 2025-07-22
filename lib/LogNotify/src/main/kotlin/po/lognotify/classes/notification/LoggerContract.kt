package po.lognotify.classes.notification

import po.lognotify.classes.notification.models.TaskData


interface LoggerContract {
    fun info(message: String): TaskData
    fun warn(message: String): TaskData
}