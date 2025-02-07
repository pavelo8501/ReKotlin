package po.exposify.components.logger

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import po.exposify.components.logger.enums.LogLevel
import java.time.LocalDateTime
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


typealias LogFunction = suspend (message: String, level: LogLevel, date: LocalDateTime, throwable: Throwable?) -> Unit

class LoggingService: ReadOnlyProperty<Any?, LoggingService>{

    private val logFunctions = mutableMapOf<LogLevel, LogFunction>()
    private val loggingServiceScope  = CoroutineScope(
        Job() + Dispatchers.IO + CoroutineName("DataService LoggingService Coroutine")
    )

    private fun log(level: LogLevel, message: String, throwable: Throwable? = null) {
        loggingServiceScope.launch {
            val timestamp = LocalDateTime.now()
            try {
                println("[$timestamp] [$level] $message")
                logFunctions.forEach { logFunction ->
                    if (level.level >= logFunction.key.level) {
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

    fun registerLogFunction(minimalLevel: LogLevel, logFunction: LogFunction){
        logFunctions[minimalLevel] = logFunction
    }

    fun clearLogFunctions(){
        logFunctions.clear()
    }

    fun info(message: String, throwable: Throwable? = null) = log(LogLevel.MESSAGE, message, throwable)
    fun action(message: String, throwable: Throwable? = null) = log(LogLevel.ACTION, message, throwable)
    fun warn(message: String, throwable: Throwable? = null) = log(LogLevel.WARNING, message, throwable)
    fun error(message: String, throwable: Throwable? = null) = log(LogLevel.EXCEPTION, message, throwable)

    override fun getValue(thisRef: Any?, property: KProperty<*>): LoggingService = this

}