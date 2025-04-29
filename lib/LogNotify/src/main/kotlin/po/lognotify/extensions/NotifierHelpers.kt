package po.lognotify.extensions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import po.lognotify.classes.notification.Notifier
import po.lognotify.classes.notification.models.Notification


suspend fun CoroutineScope.subscribeTo(notifier: Notifier, notificationFn: (suspend (Notification)-> Unit)? =null) {

    launch {
            notifier.notification.collect { notification ->
            notificationFn?.invoke(notification)
        }
    }
}