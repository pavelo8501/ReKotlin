package po.lognotify.classes.process

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import po.lognotify.classes.task.RootTask
import po.lognotify.classes.task.TaskSealedBase
import po.lognotify.extensions.subscribeTo
import po.lognotify.models.TaskKey
import kotlin.coroutines.CoroutineContext


class LoggProcess<E: ProcessableContext<*>>(private val holder: ProcessableContext<E>) : CoroutineContext.Element{

   private val listenerJobs = mutableMapOf<TaskKey, Job>()

   suspend fun observeTask(task: TaskSealedBase<*>){
       if (task is RootTask<*>) {
           println("Process Started")
           holder.onProcessStart(this)
           val job = CoroutineScope(CoroutineName("Listener")).launch {
               coroutineScope {
                   println("Reading notifier")
                   subscribeTo(task.notifier){
                       println("Notification received")
                       holder.onNotification(it)
                   }
               }
           }
           listenerJobs[task.taskData.taskKey] = job

       }
    }

    fun stopTaskObservation(task: TaskSealedBase<*>){
        if(task is RootTask<*>) {
            val job = listenerJobs[task.taskData.taskKey]
            job?.invokeOnCompletion {
                listenerJobs.remove(task.taskData.taskKey)
            }
            holder.onProcessEnd(this)
        }
    }
    override val key: CoroutineContext.Key<LoggProcess<*>> = Key
    companion object Key : CoroutineContext.Key<LoggProcess<*>>
}


