package po.lognotify

import po.lognotify.classes.notification.NotifierHub
import po.lognotify.classes.notification.models.NotifyConfig
import po.lognotify.models.TaskDispatcher

class LogNotifyHandler internal constructor(val dispatcher: TaskDispatcher) {

    val notifierHub: NotifierHub get() = dispatcher.notifierHub

    fun notifierConfig(block: NotifyConfig.()-> Unit){
        notifierHub.config.block()
    }

}