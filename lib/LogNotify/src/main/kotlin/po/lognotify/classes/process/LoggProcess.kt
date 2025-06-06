package po.lognotify.classes.process

import kotlinx.coroutines.AbstractCoroutine
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import po.lognotify.classes.notification.ProcessNotifier
import po.lognotify.classes.notification.models.NotifyConfig
import po.lognotify.classes.notification.sealed.ProviderProcess
import po.lognotify.classes.task.RootTask
import po.lognotify.models.TaskKey
import po.misc.exceptions.CoroutineInfo
import po.misc.time.ExecutionTimeStamp
import po.misc.time.MeasuredContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.coroutineContext


interface ProcessScope: CoroutineScope{

}

@OptIn(InternalCoroutinesApi::class)
class  ProcessScopeImpl<T: ProcessableContext<E>, E: CoroutineContext.Element>(
    context: CoroutineContext,
    coroutineElement : T
): AbstractCoroutine<Unit>(context, initParentJob = true, active = true), ProcessScope {

}

public fun <T: ProcessableContext<E>, E: CoroutineContext.Element> processScope(
    context: CoroutineContext = EmptyCoroutineContext,
    coroutineElement : T
): ProcessScope
{
    var scope: ProcessScopeImpl<T, E>? = null
    return ProcessScopeImpl<T, E>(context, coroutineElement).also { scope = it }
}

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

    suspend fun observeTask(task: RootTask<*, *>) {
        holder.onProcessStart(this)
        CoroutineScope(CoroutineName("Listener")).launch {
            task.notifier.notifications.collect { notification ->
                holder.onNotification(notification)
            }
        }
    }

    fun stopTaskObservation(task: RootTask<*, *>) {
        val job = listenerJobs[task.key]
        job?.invokeOnCompletion {
            listenerJobs.remove(task.key)
        }
        holder.onProcessEnd(this)
    }

    override val key: CoroutineContext.Key<LoggProcess<*>> = Key

    companion object Key : CoroutineContext.Key<LoggProcess<*>>
}


