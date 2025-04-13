package po.lognotify.logging

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import po.lognotify.enums.SeverityLevel
import java.time.LocalDateTime
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

typealias LogFunction = suspend (
    message: String,
    level: SeverityLevel,
    date: LocalDateTime,
    throwable: Throwable?) -> Unit

class LoggingService: ReadOnlyProperty<Any?, LoggingService>{

    private val logFunctions = mutableMapOf<SeverityLevel, LogFunction>()
    private val loggingServiceScope  = CoroutineScope(
        Job() + Dispatchers.IO + CoroutineName("DataService LoggingService Coroutine")
    )

    private fun log(level: SeverityLevel, message: String, throwable: Throwable? = null) {
        loggingServiceScope.launch {
            val timestamp = LocalDateTime.now()
            try {
                println("[$timestamp] [$level] $message")
                logFunctions.forEach { logFunction ->
                    if (level.severityLevelId >= logFunction.key.severityLevelId) {
                        logFunction.value(message, level, timestamp, throwable)
                        throwable?.let {
                            println("Exception: ${it.message}")
                            it.printStackTrace()
                        }
                    }
                }
            } catch (e: Exception) {
                println("[$timestamp] [ERROR] Logging failed: ${e.message}")
            }
        }
    }

    fun registerLogFunction(minimalLevel: SeverityLevel, logFunction: LogFunction){
        logFunctions[minimalLevel] = logFunction
    }

    fun clearLogFunctions(){
        logFunctions.clear()
    }

    fun info(message: String, throwable: Throwable? = null) = log(SeverityLevel.INFO, message, throwable)
    fun action(message: String, throwable: Throwable? = null) = log(SeverityLevel.INFO, message, throwable)
    fun warn(message: String, throwable: Throwable? = null) = log(SeverityLevel.WARNING, message, throwable)
    fun error(message: String, throwable: Throwable? = null) = log(SeverityLevel.EXCEPTION, message, throwable)

    override fun getValue(thisRef: Any?, property: KProperty<*>): LoggingService = this

}