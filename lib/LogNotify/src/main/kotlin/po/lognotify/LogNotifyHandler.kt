package po.lognotify

import po.lognotify.classes.notification.LoggerDataProcessor
import po.lognotify.classes.notification.NotifierHub
import po.lognotify.classes.notification.models.NotifyConfig
import po.lognotify.models.TaskDispatcher

class LogNotifyHandler internal constructor(val dispatcher: TaskDispatcher) {
    val notifierHub: NotifierHub get() = dispatcher.notifierHub

    val dataProcessor: LoggerDataProcessor get() = dispatcher.getActiveDataProcessor()

    fun notifierConfig(block: NotifyConfig.()-> Unit){
        notifierHub.sharedConfig.block()
    }


}