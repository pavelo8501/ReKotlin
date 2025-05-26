package po.lognotify.extensions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import po.lognotify.classes.notification.RootNotifier
import po.lognotify.classes.notification.models.Notification


suspend fun CoroutineScope.subscribeTo(notifier: RootNotifier<*>, notificationFn: (suspend (Notification)-> Unit)? =null) {

    launch {
            notifier.notifications.collect { notification ->
            notificationFn?.invoke(notification)
        }
    }
}