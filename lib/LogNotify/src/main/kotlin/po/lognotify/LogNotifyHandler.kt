package po.lognotify

import po.lognotify.classes.notification.NotifierHub
import po.lognotify.classes.notification.models.NotifyConfig
import po.lognotify.models.TaskDispatcher

class LogNotifyHandler internal constructor(val dispatcher: TaskDispatcher) {

    val notifier: NotifierHub get()= dispatcher.notifier

    fun notifierConfig(block: NotifyConfig.()-> Unit){
        notifier.config.block()
    }

}