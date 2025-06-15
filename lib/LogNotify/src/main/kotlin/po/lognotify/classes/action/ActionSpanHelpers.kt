package po.lognotify.classes.action

import po.lognotify.classes.task.TaskBase
import po.misc.interfaces.Identifiable
import po.misc.interfaces.IdentifiableContext


fun<T: InlineAction, R> TaskBase<*,*>.actionSpan(identifiable: IdentifiableContext, actionName: String,  receiver:T, result:R):R{
   val newActionSpan = ActionSpan(identifiable, actionName, this.key, receiver, result)
   addActionSpan(newActionSpan)
   return result
}