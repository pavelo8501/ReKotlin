package po.lognotify.classes.action

import po.lognotify.LoggableContext
import po.lognotify.TaskProcessor
import po.lognotify.TasksManaged
import po.lognotify.anotations.LogOnFault
import po.lognotify.classes.task.TaskHandler
import po.lognotify.exceptions.handleException
import po.lognotify.extensions.getOrLoggerException
import po.misc.interfaces.Identifiable
import po.misc.interfaces.IdentifiableContext
import po.misc.reflection.properties.takePropertySnapshot

interface InlineAction:  LoggableContext {

    val actionHandler: TaskHandler<*> get() {
        val message = """ActionSpan runActionSpan resulted in failure. Unable to get task handler. No active tasks in context.
        Make sure that logger tasks were started before calling this method.
    """.trimMargin()

       val availableRoot =  TasksManaged.LogNotify.taskDispatcher.activeRootTask().getOrLoggerException(message)
       return availableRoot.registry.getLastSubTask()?.handler?:availableRoot.handler
    }

    fun <T: Any, R>  runInlineAction(actionName: String, receiver: T,  block: (TaskHandler<*>)->R):R{
        val activeTask  = this.actionHandler
        val newActionSpan = ActionSpan(actionName, activeTask.task.key, this)
        return try {
            block.invoke(activeTask)
        }catch (ex: Throwable){
           // val snapshot = takePropertySnapshot<T, LogOnFault>(receiver)
          //  newActionSpan.handleException(ex,  activeTask.task, snapshot)
            throw ex
        }
    }
}

