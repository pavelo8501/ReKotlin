package po.lognotify.classes.notification.models

data class NotifyConfig(
    var muteConsole: Boolean = false,
    var muteConsoleNoEvents : Boolean = false,
    var muteInfo: Boolean = false,
    var muteWarning: Boolean = false,
    var muteException: Boolean = false
){



}