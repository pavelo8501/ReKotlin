package po.lognotify

import po.lognotify.notification.LoggerDataProcessor
import po.lognotify.notification.NotifierHub
import po.lognotify.notification.models.NotifyConfig
import po.lognotify.models.TaskDispatcher

class LogNotifyHandler internal constructor(val dispatcher: TaskDispatcher) {
    val notifierHub: NotifierHub get() = dispatcher.notifierHub

    val dataProcessor: LoggerDataProcessor get() = dispatcher.getActiveDataProcessor()

    fun notifierConfig(block: NotifyConfig.()-> Unit){
        notifierHub.sharedConfig.block()
    }


}