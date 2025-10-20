package po.misc.functions.models


data class NotificationConfig(
   internal var warnNoSubscriber: Boolean = false,
   internal var warnSubscriptionOverwritten: Boolean = true,
   internal var warnSubscriptionFailed: Boolean = true,
   internal var warnOnValueRewrite: Boolean = false,
) {

    fun warnNoSubscriber(flag: Boolean):NotificationConfig{
        warnNoSubscriber = flag
        return this
    }

    fun warnSubscriptionOverwritten(flag: Boolean):NotificationConfig{
        warnSubscriptionOverwritten = flag
        return this
    }

    fun warnSubscriptionFailed(flag: Boolean):NotificationConfig{
        warnSubscriptionFailed = flag
        return this
    }

    fun warnOnValueRewrite(flag: Boolean):NotificationConfig{
        warnOnValueRewrite = flag
        return this
    }
}