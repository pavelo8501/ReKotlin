package po.lognotify.classes.process

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import po.lognotify.classes.notification.ProcessNotifier
import po.lognotify.classes.notification.models.NotifyConfig
import po.lognotify.classes.notification.sealed.DataProvider
import po.lognotify.classes.notification.sealed.ProviderProcess
import po.lognotify.classes.task.RootTask
import po.lognotify.classes.task.TaskSealedBase
import po.lognotify.extensions.subscribeTo
import po.lognotify.models.TaskKey
import po.misc.exceptions.CoroutineInfo
import po.misc.time.ExecutionTimeStamp
import po.misc.time.MeasuredContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext


class LoggProcess<E: ProcessableContext<*>>(
    private val holder: ProcessableContext<E>,
    private val context: CoroutineContext
) : CoroutineContext.Element, MeasuredContext {

    val identifiedAs : String get() = holder.identifiedAs
    val name : String get() = holder.name
    val dataProvider = ProviderProcess(this)

    var coroutineInfo : CoroutineInfo = CoroutineInfo.createInfo(context)
    private val listenerJobs = mutableMapOf<TaskKey, Job>()

    val notifier : ProcessNotifier = ProcessNotifier(NotifyConfig(), this)
    override val executionTimeStamp: ExecutionTimeStamp = ExecutionTimeStamp(name, identifiedAs)

    init {
        initialize()
    }

    suspend fun getCoroutineInfo(): CoroutineInfo{
      return CoroutineInfo.createInfo(coroutineContext)
    }

   suspend fun startRun(context: CoroutineContext){
       coroutineInfo = CoroutineInfo.createInfo(context)
    }

    fun stopRun(): LoggProcess<E>{
        return this
    }

    fun initialize(){
        executionTimeStamp.onStart {

        }

        executionTimeStamp.onStop {

        }
        holder.getLoggerProcess = {
            this
        }
    }

    suspend fun observeTask(task: TaskSealedBase<*>) {
        if (task is RootTask<*>) {
            println("Process Started")
            holder.onProcessStart(this)
            CoroutineScope(CoroutineName("Listener")).launch {
                task.notifier.notification.collect { notification ->
                    holder.onNotification(notification)
                }
            }
        }
    }

    fun stopTaskObservation(task: TaskSealedBase<*>) {
        if (task is RootTask<*>) {
            val job = listenerJobs[task.key]
            job?.invokeOnCompletion {
                listenerJobs.remove(task.key)
            }
            holder.onProcessEnd(this)
        }
    }

    override val key: CoroutineContext.Key<LoggProcess<*>> = Key

    companion object Key : CoroutineContext.Key<LoggProcess<*>>
}


