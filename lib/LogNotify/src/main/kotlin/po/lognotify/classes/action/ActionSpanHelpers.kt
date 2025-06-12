package po.lognotify.classes.action

import po.lognotify.classes.task.TaskBase
import po.misc.interfaces.Identifiable


fun<T: InlineAction, R> TaskBase<*,*>.actionSpan(identifiable: Identifiable, actionName: String,  receiver:T, result:R):R{
   val newActionSpan = ActionSpan(identifiable, actionName, this.key, receiver, result)
   addActionSpan(newActionSpan)
   return result
}