package po.lognotify.classes.task

import po.lognotify.classes.task.models.TaskConfig
import po.lognotify.models.TaskKey

fun createTaskKey(name: String, moduleName: String, nestingLevel: Int = 0): TaskKey{
    return TaskKey(name, nestingLevel, moduleName)
}

fun <T, R: Any?>  TaskBase<*, *>.createChild(
    name: String,
    moduleName: String,
    config: TaskConfig,
    receiver: T
): Task<T, R>
{
    val thisTask = this
  return  when(thisTask){
        is RootTask->{
            val subTask = Task<T, R>(createTaskKey(name, moduleName, registry.taskCount() +1 ), config, thisTask, thisTask, receiver)
            thisTask.registry.registerChild(subTask)
            thisTask.onChildCreated(subTask)
            subTask
        }
        is Task<*, *> ->{
            val rootTask = thisTask.hierarchyRoot
            val subTaskKey = createTaskKey(name, moduleName, rootTask.registry.taskCount() +1 )
            val subTask =  Task<T, R>(subTaskKey, config, this, rootTask, receiver)
            rootTask.onChildCreated(subTask)
            subTask
        }
    }
}