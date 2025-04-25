package po.lognotify.classes.process

import po.lognotify.classes.notification.models.Notification
import kotlin.coroutines.CoroutineContext

interface ProcessableContext<out E: CoroutineContext.Element> : CoroutineContext.Element {

    fun onNotification(notification: Notification)
    fun onProcessStart(session: LoggProcess<*>)
    fun onProcessEnd(session: LoggProcess<*>)
}

