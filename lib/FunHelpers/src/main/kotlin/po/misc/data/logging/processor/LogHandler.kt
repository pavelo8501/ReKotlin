package po.misc.data.logging.processor

import po.misc.data.logging.Loggable
import po.misc.data.logging.StructuredLoggable
import kotlin.reflect.KClass

interface LogHandler {

    val baseClassHandled: KClass<out StructuredLoggable>
    fun processData (data : StructuredLoggable)


}