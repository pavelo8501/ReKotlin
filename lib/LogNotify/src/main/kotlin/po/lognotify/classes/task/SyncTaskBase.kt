package po.lognotify.classes.task

import po.lognotify.classes.notification.NotifierBase
import po.lognotify.classes.notification.RootNotifier
import po.lognotify.classes.notification.SubNotifier

import po.lognotify.classes.task.interfaces.ChildTask
import po.lognotify.classes.task.interfaces.ResultantTask
import po.lognotify.classes.task.interfaces.TopTask
import po.lognotify.exceptions.ExceptionHandler
import po.lognotify.exceptions.ExceptionHandlerSync
import po.lognotify.models.TaskKey
import po.lognotify.models.TaskRegistry
import po.lognotify.models.TaskRegistrySync
import po.misc.exceptions.CoroutineInfo
import po.misc.time.ExecutionTimeStamp
import po.misc.time.MeasuredContext


data class TaskId(
    val id : Int = 0,
    val name: String= "",
    val nestingLevel: Int = 0,
    val moduleName: String = "",
    val coroutineInfo : CoroutineInfo = CoroutineInfo("",0,"","",0,emptyList())

)

sealed class TaskBaseSync<R: Any?>(): ResultantTask {
    val taskId: TaskId = TaskId()
    override val key: TaskKey = TaskKey(taskId.name,taskId.nestingLevel, taskId.moduleName)
    abstract var taskResult: TaskResultSync<R>
    abstract  val notifier : NotifierBase
    override val executionTimeStamp: ExecutionTimeStamp = ExecutionTimeStamp(taskId.name, taskId.id.toString())
    abstract override val taskHandler: TaskHandlerBase<*>
}

class RootTaskSync<R: Any?>(
    override val key: TaskKey
): TaskBaseSync<R>(), TopTask<R>{

    override var taskResult : TaskResultSync<R> = TaskResultSync(this)
    override val registry: TaskRegistrySync<R> = TaskRegistrySync(this)
    override val notifier : RootNotifier  = RootNotifier(this)
    val exceptionHandler : ExceptionHandlerSync<R> = ExceptionHandlerSync<R>(this)
    override val subTasksCount : Int get() = registry.childTasks.count()
    override val isComplete: Boolean = false

    override val taskHandler: RootSyncTaskHandler<R> = RootSyncTaskHandler(this, exceptionHandler)

    fun <R> createNewMemberTask(name : String, moduleName: String): ChildTask{
       return registry.registerChild(SubTaskSync<R>(this, createTaskKey(name, moduleName, registry.getCount())))
    }

    fun createHandler(): RootSyncTaskHandler<R>{
        return RootSyncTaskHandler(this, ExceptionHandlerSync<R>(this))
    }
}

class SubTaskSync<R: Any?>(
    val rootTask : RootTaskSync<*>,
    override val key: TaskKey
): TaskBaseSync<R>(), ChildTask{

    override var taskResult : TaskResultSync<R> = TaskResultSync(this)
    override val notifier : SubNotifier  = SubNotifier(this, rootTask.notifier)
    val exceptionHandler : ExceptionHandlerSync<R> = ExceptionHandlerSync(this)
    override val taskHandler: SyncTaskHandler<R> = SyncTaskHandler(this, exceptionHandler)

    fun createHandler(): SyncTaskHandler<R>{
        return SyncTaskHandler(this, ExceptionHandlerSync<R>(this))
    }
}


fun createTaskKey(name: String, moduleName: String, nestingLevel: Int = 0): TaskKey{
   return TaskKey(name, nestingLevel, moduleName)
}


