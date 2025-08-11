package po.lognotify.process

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
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


class Process<T>(
    val processKey: ProcessKey<T>,
    override val receiver: T,
): LoggerProcess<T>, CoroutineContext.Element, CoroutineHolder, CTX where T: CTX, T: LogCollector, T: CoroutineContext.Element{

    override val identity: CTXIdentity<Process<T>> = asIdentity()

    val processName: String = processKey.processName

    override val key: CoroutineContext.Key<Process<*>> = Key
    val scope: CoroutineScope = CoroutineScope(this + receiver + processKey.coroutineName)
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

    override fun <T: CoroutineContext.Element> getCoroutineElement(key: CoroutineContext.Key<T>): T? =
        coroutineContext[key]

    companion object Key : CoroutineContext.Key<Process<*>>
}