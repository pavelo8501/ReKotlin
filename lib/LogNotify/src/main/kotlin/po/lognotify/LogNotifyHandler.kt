package po.lognotify

import po.lognotify.classes.notification.RootNotifier
import po.lognotify.classes.notification.models.NotifyConfig

class LogNotifyHandler internal constructor(private val notifier: RootNotifier) {

    fun notifierConfig(block: NotifyConfig.()-> Unit){
        notifier.config.block()
    }

}