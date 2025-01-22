package po.lognotify.eventhandler.exceptions



sealed class HandledThrowable(message: String, cause: Throwable?) : Throwable(message, cause) {

}