package po.lognotify.classes.action

import po.lognotify.anotations.LogOnFault
import po.lognotify.classes.task.TaskBase
import po.lognotify.classes.task.TaskHandler
import po.lognotify.exceptions.handleException
import po.misc.interfaces.Identifiable
import po.misc.interfaces.IdentifiableContext
import po.misc.reflection.properties.takePropertySnapshot

/**
 * receiver:T is a subtype of InlineAction interface
 * InlineAction implements IdentifiableContext interface
 */




fun <T:InlineAction, R>  T.runInlineAction(actionName: String,  block: (TaskHandler<*>)->R):R{
   val activeTask  = this.actionHandler
   val newActionSpan = ActionSpan(actionName, activeTask.task.key, this)
   return try {
      block.invoke(activeTask)
   }catch (ex: Throwable){
     // val snapshot = takePropertySnapshot<T, LogOnFault>(this)
     // throw newActionSpan.handleException(ex,  activeTask.task, snapshot)
      throw  ex
   }
}