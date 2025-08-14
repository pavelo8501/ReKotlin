package po.lognotify

import po.lognotify.notification.LoggerDataProcessor
import po.lognotify.notification.NotifierHub
import po.lognotify.notification.models.NotifyConfig
import po.lognotify.dispatcher.TaskDispatcher

class LogNotifyHandler internal constructor(val dispatcher: TaskDispatcher) {

   internal val logger: LoggerDataProcessor get() = dispatcher.getActiveDataProcessor()

    val notifierHub: NotifierHub get() = dispatcher.notifierHub
    fun notifierConfig(block: NotifyConfig.()-> Unit){
        notifierHub.sharedConfig.block()
    }
}