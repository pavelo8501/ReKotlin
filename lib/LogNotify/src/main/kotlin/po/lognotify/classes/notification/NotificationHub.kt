package po.lognotify.classes.notification

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import po.lognotify.classes.notification.models.NotifyConfig
import po.lognotify.classes.notification.models.TaskData
import po.lognotify.classes.task.RootTask
import po.misc.callbacks.manager.callbackManager
import po.misc.data.PrintableBase
import po.misc.data.processors.TypedDataProcessorBase
import po.misc.interfaces.Identifiable
import po.misc.interfaces.ValueBased
import po.misc.registries.callback.TypedCallbackRegistry


class NotifierHub(
    val config : NotifyConfig = NotifyConfig()
) : TypedDataProcessorBase<TaskData>() {

    enum class Event(override val value: Int): ValueBased{
        DataReceived(1)
    }

    override val topEmitter: LoggerDataProcessor? = null
    private val subNotifiers = mutableSetOf<LoggerDataProcessor>()
    private val collectorJobs = mutableMapOf<LoggerDataProcessor, Job>()

    private val notificator: TypedCallbackRegistry<PrintableBase<*>, Unit> = TypedCallbackRegistry()

    private val notifier = callbackManager<Event>()

    fun subscribe(subscriber: Identifiable, eventType:Event, callback: (PrintableBase<*>)-> Unit){
        notificator.subscribe(subscriber, eventType, callback)
    }

    internal fun register(rootTask: RootTask<*,*>){

        subNotifiers.add( rootTask.dataProcessor)

        val job = CoroutineScope(Dispatchers.Default)
        rootTask.dataProcessor.subscribeToDataEmissions(job){

            notificator.triggerForAll(Event.DataReceived, it)
        }

    }

    internal fun unregister(rootTask: RootTask<*, *>) {
        subNotifiers.remove(rootTask.dataProcessor)
        collectorJobs.remove(rootTask.dataProcessor)?.cancel()
    }


}