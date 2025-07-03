package po.lognotify.classes.action

import po.lognotify.anotations.LogOnFault
import po.lognotify.classes.task.TaskHandler
import po.lognotify.exceptions.handleException
import po.misc.exceptions.HandlerType
import po.misc.reflection.classes.ClassInfo
import po.misc.reflection.classes.ClassRole
import po.misc.reflection.classes.overallInfo
import po.misc.reflection.classes.overallInfoFromType
import po.misc.reflection.properties.takePropertySnapshot
import kotlin.reflect.KType

/**
 * receiver:T is a subtype of InlineAction interface
 * InlineAction implements IdentifiableContext interface
 */


inline fun <T: InlineAction, R> actionRunner(actionSpan: ActionSpan<T>, exceptionCase:()-> ClassInfo<R>,  block:()->R):R{
   try {
      return block.invoke()
   }catch (ex: Throwable){
      val snapshot = takePropertySnapshot<T, LogOnFault>(actionSpan.ctx)
      val managed = handleException(ex, actionSpan.taskHandler.task, snapshot, actionSpan)
      val classInfo = exceptionCase.invoke()
      val can = classInfo.canSubstituteWithNull()
      return if (managed.handler == HandlerType.SkipSelf && can) {
         null as R
      }else{
         throw managed
      }
   }
}

inline fun <T:InlineAction, reified R>  T.runInlineAction(actionName: String,  block: (TaskHandler<*>)->R):R {
   val newActionSpan = ActionSpan(actionName, actionHandler, this)
   actionHandler.task.addActionSpan(newActionSpan)
  return actionRunner(newActionSpan, { overallInfo<R>(ClassRole.Result)}){
      block.invoke(actionHandler)
   }
}

fun <T:InlineAction, R: Any?>  T.runAction(actionName: String, resultType: KType, block: (TaskHandler<*>)->R):R {
   val newActionSpan = ActionSpan(actionName, actionHandler, this)
   actionHandler.task.addActionSpan(newActionSpan)
   return actionRunner(newActionSpan, { overallInfoFromType<R>(ClassRole.Result, resultType)} ){
      block.invoke(actionHandler)
   }
}

/***
 * Short alias for runInlineAction
 */
inline fun <T:InlineAction, reified R>  T.action(actionName: String,  block: (TaskHandler<*>)->R):R
   = runInlineAction(actionName, block)

