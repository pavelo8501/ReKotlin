package po.lognotify.exceptions

import po.lognotify.classes.notification.enums.EventType
import po.lognotify.classes.notification.models.Notification
import po.lognotify.classes.notification.sealed.ProviderHandler
import po.lognotify.classes.task.ControlledTask
import po.lognotify.classes.task.ManagedTask
import po.lognotify.classes.task.TaskSealedBase
import po.lognotify.enums.SeverityLevel
import po.lognotify.exceptions.ExceptionHandler.HandlerResult
import po.lognotify.exceptions.enums.HandlerType

interface ExceptionHandled<R: Any?> {

   suspend fun subscribeHandlerUpdates(callback: suspend (notification: Notification) -> Unit)
   suspend fun handleManaged(managedEx: ManagedException): HandlerResult<R>
   suspend fun provideHandlerFn(handler : HandlerType, handlerFn:()->R)

}


class ExceptionHandler<R: Any?>(
   private val task : ControlledTask
) : ExceptionHandled<R> {

   class HandlerResult<R: Any?>(
      val value:R? = null,
      val exception: ManagedException? = null,
   ){

      val success: Boolean = exception!=null

   }

   private var onHandlerUpdate: (suspend (notification: Notification) -> Unit)? = null
   override suspend fun subscribeHandlerUpdates(callback: suspend (notification: Notification) -> Unit){
      onHandlerUpdate = callback
   }

   private suspend fun notifyHandlerSet(handler : HandlerType){
      val message = "$handler handle set"
      val notification = Notification(
         task,
         EventType.HANDLER_REGISTERED,
         SeverityLevel.INFO,
         message,
         ProviderHandler(task.key.taskName
         ))
      onHandlerUpdate?.invoke(notification)
   }
   private suspend fun notifyHandled(managedEx : ManagedException){
      val message =  "Exception Handled. Exception Message : ${managedEx.message}"
      val severity = SeverityLevel.WARNING

      val notification = Notification(
         task,
         EventType.EXCEPTION_UNHANDLED,
         severity,
         message,
         ProviderHandler(task.taskName))
       onHandlerUpdate?.invoke(notification)
   }
   private suspend fun notifyUnhandled(managedEx : ManagedException){
      val severity = SeverityLevel.EXCEPTION
      val message =  "Unhandled  : ${managedEx.message}"
      val notification = Notification(
         task,
         EventType.EXCEPTION_UNHANDLED,
         severity,
         message,
         ProviderHandler(task.taskName))
      onHandlerUpdate?.invoke(notification)
   }

   val handlers : MutableMap<HandlerType, ()->R> = mutableMapOf()

   override suspend fun provideHandlerFn(handler : HandlerType, handlerFn:()->R){
      handlers[handler] = handlerFn
      notifyHandlerSet(handler)
   }

   override suspend fun handleManaged(managedEx: ManagedException): HandlerResult<R> {
      val handlerFn = handlers[managedEx.handler]
      val handlingResult = handlerFn?.let {
           notifyHandled(managedEx)
           HandlerResult<R>(handlerFn.invoke())
        } ?:run {
            notifyUnhandled(managedEx)
            HandlerResult<R>(null, managedEx)
         }
         return handlingResult
   }
}