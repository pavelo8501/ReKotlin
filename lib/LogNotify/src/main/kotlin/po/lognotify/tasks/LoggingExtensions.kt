package po.lognotify.tasks

import po.misc.context.CTX
import po.misc.data.processors.SeverityLevel


internal fun <T: CTX, R: Any?>  TaskBase<T, R>.warn(message: String){
    dataProcessor.notify(message, SeverityLevel.WARNING, this)
}

internal fun <T: CTX, R: Any?>  TaskBase<T, R>.info(message: String){
    dataProcessor.notify(message, SeverityLevel.INFO, this)
}