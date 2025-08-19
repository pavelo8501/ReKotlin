package po.lognotify.process

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import po.lognotify.notification.models.LogData
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.coroutines.HotFlowEmitter
import po.misc.functions.registries.builders.notifierRegistryOf
import po.misc.functions.registries.builders.subscribe
import po.misc.functions.registries.builders.taggedRegistryOf
import po.misc.interfaces.Processable
import po.misc.time.ExecutionTimeStamp
import kotlin.coroutines.CoroutineContext


enum class ProcessEvents{
    DataReceived
}

enum class ProcessStatus{
    Active,
    Complete
}

class Process<T>(
    val processKey: ProcessKey<T>,
    val receiver: T,
    val dispatcher: CoroutineDispatcher,
): LoggerProcess<T> where T: Processable{

    override val identity: CTXIdentity<Process<T>> = asIdentity()

    override val handler: ProcessHandler = ProcessHandler(this)

    private val listenerScope =  CoroutineScope(CoroutineName("Listener"))

    val processName: String = processKey.processName
    var status:ProcessStatus = ProcessStatus.Active

    override val key: CoroutineContext.Key<Process<*>> = Key
    override val scope: CoroutineScope = CoroutineScope(this + dispatcher  + receiver + processKey.coroutineName)
    val coroutineContext: CoroutineContext get() = scope.coroutineContext

    val timeStamp: ExecutionTimeStamp = ExecutionTimeStamp(processName, "LoggerProcess")

    internal val onComplete = notifierRegistryOf<Process<T>>(ProcessStatus.Complete)
    internal val dataNotifier = taggedRegistryOf<ProcessEvents, LogData>()

    init {
        timeStamp.startTimer()
    }

    private fun processDataReceived(data : LogData){
        receiver.provideData(data)
        dataNotifier.trigger(data)
    }

    internal fun <R> complete(result:R):R{
        status = ProcessStatus.Complete
        onComplete.trigger(this)
        onComplete.clear()
        dataNotifier.clear()
        return result
    }

    override fun CTX.onDataReceived(callback: (LogData) -> Unit): Unit =
        subscribe(ProcessEvents.DataReceived, dataNotifier, callback)

    fun observeTask(flowEmitter: HotFlowEmitter<LogData>) {
        flowEmitter.collectEmissions(listenerScope) { data ->
            processDataReceived(data)
        }
    }

    override fun <T: CoroutineContext.Element> getCoroutineElement(key: CoroutineContext.Key<T>): T? =
        coroutineContext[key]

    companion object Key : CoroutineContext.Key<Process<*>>
}