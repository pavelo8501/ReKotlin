package po.lognotify.process


import kotlin.coroutines.CoroutineContext

interface ProcessableContext<out E: CoroutineContext.Element> : CoroutineContext.Element {

    val name : String
    val identifiedAs: String

    var getLoggerProcess:  (()-> LoggerProcess<*, *>)?

    fun onProcessStart(session: LoggerProcess<*, *>)
    fun onProcessEnd(session: LoggerProcess<*, *>)
}