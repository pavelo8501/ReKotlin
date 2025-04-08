package po.lognotify.classes.notification.sealed

data class ProviderTask(val taskName: String) :InfoProvider(taskName)
data class ProviderThrower(val taskName: String) :InfoProvider(taskName)
data class ProviderHandler(val taskName: String) :InfoProvider(taskName)

sealed class InfoProvider(var name: String) {

}