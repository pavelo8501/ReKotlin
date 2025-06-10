package po.lognotify.classes.notification

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import po.lognotify.classes.notification.models.NotifyConfig
import po.lognotify.classes.notification.models.TaskData
import po.lognotify.classes.task.RootTask
import po.misc.data.PrintableBase
import po.misc.data.processors.TypedDataProcessorBase
import po.misc.interfaces.Identifiable
import po.misc.interfaces.ValueBased
import po.misc.registries.callback.TypedCallbackRegistry


class NotifierHub(
    val config : NotifyConfig = NotifyConfig()
) : TypedDataProcessorBase<TaskData>() {

    enum class EventType(override val value: Int): ValueBased{
        NewEvent(1)
    }

    override val topEmitter: LoggerDataProcessor? = null
    private val subNotifiers = mutableSetOf<LoggerDataProcessor>()
    private val collectorJobs = mutableMapOf<LoggerDataProcessor, Job>()

    private val notificator: TypedCallbackRegistry<PrintableBase<*>, Unit> = TypedCallbackRegistry()

    fun subscribe(subscriber: Identifiable, eventType:EventType, callback: (PrintableBase<*>)-> Unit){
        notificator.subscribe(subscriber, eventType, callback)
    }

    internal fun register(rootTask: RootTask<*,*>){
        val rootTaskDataProcessor = rootTask.dataProcessor
        subNotifiers.add(rootTaskDataProcessor)
        val job = CoroutineScope(Dispatchers.Default).launch {
            rootTaskDataProcessor.notifications.collect {
                notificator.triggerForAll(EventType.NewEvent, it)
            }
        }
        collectorJobs[rootTaskDataProcessor] = job
    }

    internal fun unregister(rootTask: RootTask<*, *>) {
        subNotifiers.remove(rootTask.dataProcessor)
        collectorJobs.remove(rootTask.dataProcessor)?.cancel()
    }


}