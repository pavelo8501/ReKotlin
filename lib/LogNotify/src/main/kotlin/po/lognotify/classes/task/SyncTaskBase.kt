package po.lognotify.classes.task

import po.lognotify.classes.notification.NotifierBase
import po.lognotify.classes.notification.RootNotifier
import po.lognotify.classes.notification.SubNotifier

import po.lognotify.classes.task.interfaces.ResultantTask
import po.lognotify.classes.task.interfaces.TopTask
import po.lognotify.classes.task.models.TaskId
import po.lognotify.classes.task.result.TaskResult
import po.lognotify.exceptions.ExceptionHandler
import po.lognotify.models.TaskKey
import po.misc.exceptions.CoroutineInfo
import po.misc.time.ExecutionTimeStamp



//
//sealed class TaskBaseSync<R: Any?>(): ResultantTask<R> {
//    val taskId: TaskId = TaskId()
//    override val key: TaskKey = TaskKey(taskId.name,taskId.nestingLevel, taskId.moduleName)
//    abstract var taskResult: TaskResult<R>
//    abstract override val notifier : NotifierBase
//    override val executionTimeStamp: ExecutionTimeStamp = ExecutionTimeStamp(taskId.name, taskId.id.toString())
//    abstract override val taskHandler: TaskHandler<R>
//}
//
//class RootTaskSync<R: Any?>(
//    override val key: TaskKey
//): TaskBaseSync<R>(), TopTask<R>, ResultantTask<R>{
//
//    override var taskResult : TaskResult<R> = TaskResult(this)
//    override val registry: TaskRegistrySync<R> = TaskRegistrySync(this)
//    override val notifier : RootNotifier  = RootNotifier(this)
//    override val exceptionHandler : ExceptionHandler<R> = ExceptionHandler<R>(this)
//    override val subTasksCount : Int get() = registry.childTasks.count()
//    override val isComplete: Boolean = false
//
//    override val taskHandler: TaskHandler<R> = TaskHandler(this, exceptionHandler)
//
//
//    fun createHandler(): RootSyncTaskHandler<R>{
//        return RootSyncTaskHandler(this, ExceptionHandler<R>(this))
//    }
//}
//
//class SubTaskSync<R: Any?>(
//    val rootTask : RootTaskSync<*>,
//    override val key: TaskKey
//): TaskBaseSync<R>(), ChildTask, ResultantTask<R>{
//
//    override var taskResult : TaskResult<R> = TaskResult(this)
//    override val notifier : SubNotifier  = SubNotifier(this, rootTask.notifier)
//    override val exceptionHandler : ExceptionHandler<R> = ExceptionHandler(this)
//    override val taskHandler: SyncTaskHandler<R> = SyncTaskHandler(this, exceptionHandler)
//
//    fun createHandler(): SyncTaskHandler<R>{
//        return SyncTaskHandler(this, ExceptionHandler<R>(this))
//    }
//}
//
//fun <R2> RootTaskSync<*>.createChild(name: String, moduleName: String):SubTaskSync<R2>{
//    val task =  SubTaskSync<R2>(this, createTaskKey(name, moduleName, registry.getCount()+1))
//    registry.registerChild(task)
//    return task
//}
//



