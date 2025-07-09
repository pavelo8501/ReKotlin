package po.lognotify.classes.action

import po.lognotify.TasksManaged
import po.lognotify.tasks.TaskHandler
import po.lognotify.extensions.getOrLoggerException

interface InlineAction:  TasksManaged {

    val actionHandler: TaskHandler<*> get() {
        val message = """ActionSpan runActionSpan resulted in failure. Unable to get task handler. No active tasks in context.
        Make sure that logger tasks were started before calling this method.
    """.trimMargin()
       val availableRoot =  TasksManaged.LogNotify.taskDispatcher.activeRootTask().getOrLoggerException(message)
       return availableRoot.registry.getLastSubTask()?.handler?:availableRoot.handler
    }

}

