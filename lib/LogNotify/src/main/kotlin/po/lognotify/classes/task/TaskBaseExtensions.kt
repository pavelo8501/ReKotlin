package po.lognotify.classes.task

import po.lognotify.classes.task.models.TaskConfig
import po.lognotify.models.TaskKey

fun createTaskKey(name: String, moduleName: String, nestingLevel: Int = 0): TaskKey{
    return TaskKey(name, nestingLevel, moduleName)
}

fun <R> RootTask<*>.createChild(name: String, moduleName: String, config: TaskConfig): Task<R>{
    val task =  Task<R>(createTaskKey(name, moduleName, registry.taskCount()+1), config, this, this)
    registry.registerChild(task)
    onChildCreated(task)
    return task
}