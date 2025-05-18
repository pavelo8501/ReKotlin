package po.lognotify

import po.lognotify.classes.notification.NotifierHub
import po.lognotify.classes.notification.models.NotifyConfig

class LogNotifyHandler internal constructor(val notifier: NotifierHub) {

    fun notifierConfig(block: NotifyConfig.()-> Unit){
        notifier.config.block()
    }

}