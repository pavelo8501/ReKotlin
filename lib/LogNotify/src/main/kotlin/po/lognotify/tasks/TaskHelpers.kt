package po.lognotify.tasks

import po.lognotify.common.configuration.TaskConfig
import po.lognotify.common.configuration.TaskType
import po.lognotify.dispatcher.createHierarchyRoot
import po.lognotify.models.TaskKey
import po.misc.context.CTX


internal fun generateRootKey(name: String, receiver: CTX): TaskKey{
   return TaskKey(name, 0, receiver.contextName)
}

internal fun createTaskKey(name: String, nestingLevel: Int, receiver: CTX): TaskKey{
    return TaskKey(name, nestingLevel, receiver.contextName)
}


/**
 * Experimental
 */
@PublishedApi
internal fun <T : CTX, R> TaskBase<*, *>?.createTask(
    name: String,
    receiver: T,
    config: TaskConfig
): TaskBase<T, R> {

    if(this != null){
        if (config.taskType == TaskType.AsRootTask) {
            return  createHierarchyRoot(name, receiver, config)
        } else {
            return   when (this) {
                is RootTask -> {
                    val taskKey = createTaskKey(name, registry.childCount + 1, receiver)
                    val subTask = Task<T, R>(taskKey, config, this, this, receiver)
                    registry.registerChild(subTask)
                    onChildCreated(subTask)
                    subTask
                }
                is Task<*, *> -> {
                    val taskKey = createTaskKey(name, hierarchyRoot.registry.childCount + 1, receiver)
                    val subTask = Task<T, R>(taskKey, config, this, hierarchyRoot, receiver)
                    hierarchyRoot.onChildCreated(subTask)
                    subTask
                }
            }
        }
    }else{
       return createHierarchyRoot(name, receiver, config)
    }
}



//@PublishedApi
//internal fun <T : CTX, R : Any?> TaskBase<*, *>.createChildTask(
//    name: String,
//    receiver: T,
//    config: TaskConfig = TaskConfig()
//): Task<T, R> =
//     when(this){
//        is RootTask->{
//            val taskKey = createTaskKey(name, registry.childCount + 1, receiver)
//            val subTask = Task<T, R>(taskKey, config, this, this, receiver)
//            registry.registerChild(subTask)
//            onChildCreated(subTask)
//            subTask
//        }
//        is Task<*, *> -> {
//            val taskKey =  createTaskKey(name, hierarchyRoot.registry.childCount + 1, receiver)
//            val subTask = Task<T, R>(taskKey, config, this, hierarchyRoot, receiver)
//            hierarchyRoot.onChildCreated(subTask)
//            subTask
//        }
//    }