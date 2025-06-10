package po.lognotify.classes.process

import po.misc.time.MeasuredContext
import kotlin.coroutines.CoroutineContext

interface ProcessableContext<out E: CoroutineContext.Element> : CoroutineContext.Element {

    val name : String
    val identifiedAs: String

    var getLoggerProcess:  (()-> LoggProcess<*>)?

    fun onProcessStart(session: LoggProcess<*>)
    fun onProcessEnd(session: LoggProcess<*>)
}

