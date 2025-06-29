package po.lognotify.extensions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

import po.lognotify.classes.task.RootTask
import po.misc.data.printable.PrintableBase


//suspend fun CoroutineScope.subscribeTo(rootTask: RootTask<*, *>, notificationFn: (suspend (PrintableBase<*>)-> Unit)? =null) {
//    launch {
//        rootTask.dataProcessor.notifications.collect {notification ->
//            notificationFn?.invoke(notification)
//        }
//    }
//}