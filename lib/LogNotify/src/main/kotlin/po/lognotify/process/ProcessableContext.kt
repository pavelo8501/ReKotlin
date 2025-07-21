package po.lognotify.process


import po.misc.data.printable.PrintableBase
import po.misc.context.Identifiable
import kotlin.coroutines.CoroutineContext

interface ProcessableContext<out E: CoroutineContext.Element> : CoroutineContext.Element {

    val name : String
    val identifiedAs: String

    var getLoggerProcess:  (()-> LoggerProcess<*, *>)?

    fun onProcessStart(session: LoggerProcess<*, *>)
    fun onProcessEnd(session: LoggerProcess<*, *>)
}


interface LogReceiver{
    val receivableContext:()->LogReceiver
    fun receiveUpdate(data: PrintableBase<*>)

}