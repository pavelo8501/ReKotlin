package po.lognotify.notification

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import po.lognotify.notification.models.NotifyConfig
import po.lognotify.notification.models.LogData
import po.lognotify.tasks.RootTask
import po.misc.callbacks.builders.callbackManager
import po.misc.context.CTX
import po.misc.data.printable.PrintableBase
import po.misc.data.processors.DataProcessorBase
import po.misc.context.asIdentity
import po.misc.interfaces.ValueBased
import po.misc.registries.callback.TypedCallbackRegistry


class NotifierHub(
    val sharedConfig : NotifyConfig = NotifyConfig()
): DataProcessorBase<LogData>(null), CTX {

    enum class Event(override val value: Int): ValueBased{
        DataReceived(1)
    }

    override val identity = asIdentity()

    private val subNotifiers = mutableSetOf<LoggerDataProcessor>()

    // private val collectorJobs = mutableMapOf<LoggerDataProcessor, Job>()

    private val notificator: TypedCallbackRegistry<PrintableBase<*>, Unit> = TypedCallbackRegistry()
    private val notifier = callbackManager<Event>()

    fun subscribe(subscriber: CTX, eventType:Event, callback: (PrintableBase<*>)-> Unit){
        notificator.subscribe(subscriber, eventType, callback)
    }

    internal fun register(rootTask: RootTask<*,*>){
        subNotifiers.add(rootTask.dataProcessor)
        val job = CoroutineScope(Dispatchers.Default)
        rootTask.dataProcessor.flowEmitter?.subscribeToDataEmissions(job){
            notificator.triggerForAll(Event.DataReceived, it)
        }
    }

    internal fun unregister(rootTask: RootTask<*, *>) {
        subNotifiers.remove(rootTask.dataProcessor)
       // collectorJobs.remove(rootTask.dataProcessor)?.cancel()
    }


}