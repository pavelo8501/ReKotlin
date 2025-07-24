package po.lognotify.tasks

import po.lognotify.enums.SeverityLevel
import po.lognotify.tasks.models.TaskConfig
import po.lognotify.models.TaskKey
import po.misc.context.CTX

fun createTaskKey(name: String, moduleName: String, nestingLevel: Int = 0): TaskKey{
    return TaskKey(name, nestingLevel, moduleName)
}


fun <T: CTX, R: Any?>  TaskBase<*, *>.createChild(
    name: String,
    moduleName: String,
    receiver: T,
    config: TaskConfig,
): Task<T, R>{
  return  when(this){
        is RootTask->{
            val subTask = Task<T, R>(createTaskKey(name, moduleName, registry.childCount + 1 ), config, this, this, receiver)
            this.registry.registerChild(subTask)
            this.onChildCreated(subTask)
            subTask
        }
        is Task<*, *> ->{
            val subTaskKey = createTaskKey(name, moduleName, hierarchyRoot.registry.childCount + 1 )
            val subTask =  Task<T, R>(subTaskKey, config, this, hierarchyRoot, receiver)
            hierarchyRoot.onChildCreated(subTask)
            subTask
        }
    }
}

fun <T: CTX, R: Any?>  TaskBase<*, *>.createChild(
    name: String,
    receiver: T,
    config: TaskConfig? = null
): Task<T, R>{
    val effectiveConfig: TaskConfig = config?:this.config
    val moduleName = receiver.identity.identifiedByName
    return  when(this){
        is RootTask->{
            val newTask = Task<T, R>(createTaskKey(name, moduleName, registry.childCount + 1), effectiveConfig, this, this, receiver)
            onChildCreated(newTask)
            newTask
        }
        is Task<*, *> ->{
            val subTaskKey = createTaskKey(name, moduleName, hierarchyRoot.registry.childCount + 1)
            val newTask =  Task<T, R>(subTaskKey, effectiveConfig, this, hierarchyRoot, receiver)
            hierarchyRoot.onChildCreated(newTask)
            newTask
        }
    }
}


internal fun <T: CTX, R: Any?>  TaskBase<T, R>.log(message: String, severity: SeverityLevel){
    dataProcessor.log(message, severity, this)
}


