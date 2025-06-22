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


inline fun <T: Any, R>  InlineAction.runInlineAction(actionName: String, receiver: T,  block: T.(TaskHandler<*>)->R):R{
   val activeTask  = this.actionHandler
   val newActionSpan = ActionSpan(actionName, activeTask.task.key, this)
   return try {
      block.invoke(receiver, activeTask)
   }catch (ex: Throwable){
      val snapshot = takePropertySnapshot<T, LogOnFault>(receiver)
      newActionSpan.handleException(ex,  activeTask.task, snapshot)
      throw ex
   }
}


inline fun <T:InlineAction, R>  T.runInlineAction(actionName: String,  block: T.(TaskHandler<*>)->R):R{
   val activeTask  = this.actionHandler
   val newActionSpan = ActionSpan(actionName, activeTask.task.key, this)
   return try {
      block.invoke(this, activeTask)
   }catch (ex: Throwable){
      val snapshot = takePropertySnapshot<T, LogOnFault>(this)
      newActionSpan.handleException(ex,  activeTask.task, snapshot)
      throw ex
   }
}