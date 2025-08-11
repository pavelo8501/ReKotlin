package po.lognotify.tasks

import po.lognotify.common.configuration.TaskConfig
import po.lognotify.models.TaskKey
import po.misc.context.CTX

fun createTaskKey(
    name: String,
    moduleName: String,
    nestingLevel: Int = 0,
): TaskKey = TaskKey(name, nestingLevel, moduleName)

fun <T : CTX, R : Any?> TaskBase<*, *>.createChild(
    name: String,
    receiver: T,
    config: TaskConfig = TaskConfig()
): Task<T, R> =
    when (this) {
        is RootTask -> {
            val subTask = Task<T, R>(createTaskKey(name, identity.className, registry.childCount + 1), config, this, this, receiver)
            this.registry.registerChild(subTask)
            this.onChildCreated(subTask)
            subTask
        }
        is Task<*, *> -> {
            val subTaskKey = createTaskKey(name, identity.className, hierarchyRoot.registry.childCount + 1)
            val subTask = Task<T, R>(subTaskKey, config, this, hierarchyRoot, receiver)
            hierarchyRoot.onChildCreated(subTask)
            subTask
        }
    }