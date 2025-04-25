package po.lognotify.extensions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import po.lognotify.classes.notification.Notifier

fun CoroutineScope.subscribeTo(notifier: Notifier) {
    launch {
        notifier.notification.collect { notification ->
            println("Received notification: $notification")
        }
    }
}