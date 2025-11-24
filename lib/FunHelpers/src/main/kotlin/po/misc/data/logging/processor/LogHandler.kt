package po.misc.data.logging.processor

import po.misc.callbacks.signal.Signal
import po.misc.data.logging.StructuredLoggable
import kotlin.reflect.KClass

interface LogHandler {
    val targetClassHandled: KClass<out StructuredLoggable>
    val completionSignal: Signal<LogHandler, Unit>
    fun processRecord(logRecord : StructuredLoggable)
}
