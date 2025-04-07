package po.lognotify.classes.notification

import po.lognotify.classes.notification.enums.EventType
import po.lognotify.classes.notification.enums.InfoProvider
import po.lognotify.classes.notification.models.Notification
import po.lognotify.classes.task.ResultantTask
import po.lognotify.classes.task.TaskHandler
import po.lognotify.classes.task.TaskSealedBase
import po.lognotify.enums.SeverityLevel


class HelperFunctions {
     fun <T: ResultantTask>  T.makeNotification(eventType : EventType, message : String): Notification {

        val notification = Notification(
            this.taskName,
            this.nestingLevel,
            eventType,
            SeverityLevel.INFO,
            message,
            InfoProvider.TASK
        )
        return notification
    }

    fun <T: TaskSealedBase<T>>  T.makeNotification(eventType : EventType, message : String): Notification {

        val notification = Notification(
            this.taskName,
            this.nestingLevel,
            eventType,
            SeverityLevel.INFO,
            message,
            InfoProvider.TASK
        )
        return notification
    }

    fun  TaskHandler.makeNotification(eventType : EventType, message : String): Notification {

        val notification = Notification(
            this.taskName,
            this.nestingLevel,
            eventType,
            SeverityLevel.INFO,
            message,
            InfoProvider.TASK
        )
        return notification
    }



}