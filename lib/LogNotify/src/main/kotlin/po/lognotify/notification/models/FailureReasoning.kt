package po.lognotify.notification.models

data class FailureReasoning(
    val lnInstanceName: String,
    val nestingLevel: Int,
    val exceptionHandled: Boolean,
)
