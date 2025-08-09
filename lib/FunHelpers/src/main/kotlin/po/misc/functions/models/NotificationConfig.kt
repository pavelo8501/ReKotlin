package po.misc.functions.models


enum class Notifications{

}


data class NotificationConfig(
   internal var warnNoSubscriber: Boolean = false,
   internal var warnSubscriptionOverwritten: Boolean = true
) {

    fun warnNoSubscriber(flag: Boolean):NotificationConfig{
        warnNoSubscriber = flag
        return this
    }

    fun warnSubscriptionOverwritten(flag: Boolean):NotificationConfig{
        warnSubscriptionOverwritten = flag
        return this
    }

}