package po.lognotify.notification

import po.lognotify.common.LNInstance
import po.lognotify.notification.models.LogData
import po.misc.data.printable.json.toJson
import po.misc.data.processors.SeverityLevel


fun LNInstance<*>.warning( message: String){
    dataProcessor.notify(this, message, SeverityLevel.WARNING)
}

fun LNInstance<*>.information(message: String){
    dataProcessor.notify(this, message, SeverityLevel.INFO)
}

fun LNInstance<*>.error(message: String){
    dataProcessor.notify(this, message, SeverityLevel.EXCEPTION)
}

fun List<LogData>.toJson(): String{
    val result = joinToString(prefix = "[", postfix = "]", separator = ",") {
        it.toJson()
    }
    return result
}