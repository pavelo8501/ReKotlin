package po.lognotify.process


import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import po.lognotify.interfaces.LoggerProcess
import po.lognotify.notification.models.LogData
import po.lognotify.tasks.RootTask
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.coroutines.CoroutineHolder
import po.misc.data.logging.LogCollector
import po.misc.data.printable.PrintableBase
import po.misc.time.ExecutionTimeStamp
import kotlin.coroutines.CoroutineContext


class LoggerProcessImplementation<T>(
    val processName: String,
    override val receiver: T,
    val contextElement : CoroutineContext.Element,
): LoggerProcess<T>, CoroutineContext.Element, CoroutineHolder, CTX where T: CTX, T: LogCollector{

    override val identity: CTXIdentity<LoggerProcessImplementation<T>> = asIdentity()

    override val key: CoroutineContext.Key<LoggerProcessImplementation<*>> = Key
    val scope: CoroutineScope = CoroutineScope(this + contextElement)
    override val coroutineContext: CoroutineContext get() = scope.coroutineContext

    val timeStamp: ExecutionTimeStamp = ExecutionTimeStamp(processName, "LoggerProcess")
    var onDataReceived: ((LogData)-> Unit)? = null

    init {
        timeStamp.startTimer()
    }

    override fun onDataReceived(callback: (PrintableBase<*>) -> Unit) {
        onDataReceived = callback
    }

   fun observeTask(task: RootTask<*, *>) {
       val flowEmitter = task.dataProcessor.flowEmitter
       if(flowEmitter != null){
           val listenerScope =  CoroutineScope(CoroutineName("Listener"))
           flowEmitter.subscribeToDataEmissions(listenerScope){data->
               receiver.provideData(data)
               onDataReceived?.invoke(data)
           }
       }
    }
    companion object Key : CoroutineContext.Key<LoggerProcessImplementation<*>>
}