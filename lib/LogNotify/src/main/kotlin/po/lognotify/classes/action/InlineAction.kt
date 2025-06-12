package po.lognotify.classes.action

import po.lognotify.TasksManaged
import po.lognotify.classes.task.TaskHandler
import po.lognotify.extensions.getOrLoggerException
import po.misc.interfaces.Identifiable

interface InlineAction : TasksManaged {

    override fun activeTaskHandler(): TaskHandler<*>{
        val message = """ActionSpan runActionSpan resulted in failure. Unable to get task handler. No active tasks in context.
        Make sure that logger tasks were started before calling this method.
    """.trimMargin()

        val availableRoot =  TasksManaged.taskDispatcher.activeRootTask().getOrLoggerException(message)
        return availableRoot.registry.getLastSubTask()?.handler?:availableRoot.handler
    }
}

inline fun <T:InlineAction,R>  T.runInlineAction(identifiable: Identifiable, actionName: String,  block: T.(TaskHandler<*>)->R):R{
    return try {
        val activeTask  = this.activeTaskHandler()
        activeTask.task.actionSpan(identifiable,actionName,  this, block.invoke(this, activeTask))
    }catch (ex: Throwable){
        throw ex
    }
}