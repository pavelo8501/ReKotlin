package po.lognotify.exceptions

import po.lognotify.classes.notification.enums.EventType
import po.lognotify.classes.notification.models.Notification
import po.lognotify.classes.notification.sealed.ProviderTask
import po.lognotify.classes.task.TaskBase
import po.lognotify.classes.task.interfaces.ResultantTask
import po.lognotify.classes.task.result.TaskResult
import po.lognotify.classes.task.result.toTaskResult
import po.lognotify.enums.SeverityLevel
import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException

interface ExceptionHandled<R: Any?> {

   fun handleManaged(managedEx: ManagedException): TaskResult<R>
  // fun provideHandlerFn(handlers : Set<HandlerType>, handlerFn: (exception: ManagedException)->R)

}

class ExceptionHandler<R: Any?>(
   private val task : TaskBase<R>,
) : ExceptionHandled<R> {

   private fun notifyHandlerSet(handler : HandlerType){
      val message = "$handler handle set"
      val notification = Notification(
         ProviderTask(task),
         EventType.HANDLER_REGISTERED,
         SeverityLevel.INFO,
         message)
      task.notifier.submitNotification(notification)
   }
   private fun notifyHandled(managedEx : ManagedException){
      val message =  "Exception Handled. Exception Message : ${managedEx.message}"
      val severity = SeverityLevel.WARNING

      val notification = Notification(
         ProviderTask(task),
         EventType.EXCEPTION_UNHANDLED,
         severity,
         message)

      task.notifier.submitNotification(notification)
   }
   private fun notifyUnhandled(managedEx : ManagedException){
      val severity = SeverityLevel.EXCEPTION

      var message = """ 
          Unhandled in Task:${task.key.taskName}| Module: ${task.key.moduleName}| Nesting: ${task.key.nestingLevel}
          Message: ${managedEx.message}
      """.trimIndent()

         val snapshotStr = managedEx.propertySnapshot.map { "${it.key} = ${it.value}" }.joinToString(";")
         message = """  
             Unhandled in Task:${task.key.taskName}| Module: ${task.key.moduleName}| Nesting: ${task.key.nestingLevel}
             Message: ${managedEx.message}
             Snapshot: $snapshotStr
         """.trimIndent()

      val notification = Notification(
         ProviderTask(task),
         EventType.EXCEPTION_UNHANDLED,
         severity,
         message)

      task.notifier.submitNotification(notification)
   }

   val registeredAsyncHandlers : MutableMap<HandlerType, suspend (exception:  ManagedException)->R> = mutableMapOf()
   val registeredHandlers : MutableMap<HandlerType, (exception:  ManagedException)->R> = mutableMapOf()

   suspend fun provideAsyncHandlerFn(handlers : Set<HandlerType>, handlerFn: suspend (exception: ManagedException)->R){
      handlers.forEach {
         registeredAsyncHandlers[it] = handlerFn
         notifyHandlerSet(it)
      }
   }

   fun provideHandlerFn(handlers : Set<HandlerType>, handlerFn: (exception: ManagedException)->R){
      handlers.forEach {
         registeredHandlers[it] = handlerFn
         notifyHandlerSet(it)
      }
   }

   override fun handleManaged(managedEx: ManagedException): TaskResult<R> {
      val handlerFn = registeredHandlers[managedEx.handler]
      return if (handlerFn != null) {
         notifyHandled(managedEx)
         val result =  handlerFn.invoke(managedEx)
         task.toTaskResult(result)
      } else {
         notifyUnhandled(managedEx)
         task.toTaskResult(managedEx)
      }
   }
}
