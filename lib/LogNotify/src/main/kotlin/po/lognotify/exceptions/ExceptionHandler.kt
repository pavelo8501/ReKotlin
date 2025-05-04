package po.lognotify.exceptions

import po.lognotify.classes.notification.enums.EventType
import po.lognotify.classes.notification.models.Notification
import po.lognotify.classes.notification.sealed.ProviderTask
import po.lognotify.classes.task.TaskSealedBase
import po.lognotify.enums.SeverityLevel
import po.lognotify.exceptions.ExceptionHandler.HandlerResult
import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException

interface ExceptionHandled<R: Any?> {

   suspend fun subscribeHandlerUpdates(callback: suspend (notification: Notification) -> Unit)
   suspend fun handleManaged(managedEx: ManagedException): HandlerResult<R>
   suspend fun provideHandlerFn(handlers : Set<HandlerType>, handlerFn: suspend (exception: ManagedException)->R)

}

class ExceptionHandler<R: Any?>(
   private val task : TaskSealedBase<R>,
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
         ProviderTask(task),
         EventType.HANDLER_REGISTERED,
         SeverityLevel.INFO,
         message)
      onHandlerUpdate?.invoke(notification)
   }
   private suspend fun notifyHandled(managedEx : ManagedException){
      val message =  "Exception Handled. Exception Message : ${managedEx.message}"
      val severity = SeverityLevel.WARNING

      val notification = Notification(
         ProviderTask(task),
         EventType.EXCEPTION_UNHANDLED,
         severity,
         message)
       onHandlerUpdate?.invoke(notification)
   }
   private suspend fun notifyUnhandled(managedEx : ManagedException){
      val severity = SeverityLevel.EXCEPTION
      val message =  "Unhandled  : ${managedEx.message}"
      val notification = Notification(
         ProviderTask(task),
         EventType.EXCEPTION_UNHANDLED,
         severity,
         message)
      onHandlerUpdate?.invoke(notification)
   }

   val registeredHandlers : MutableMap<HandlerType, suspend ( exception: ManagedException)->R> = mutableMapOf()

   override suspend fun provideHandlerFn(handlers : Set<HandlerType>, handlerFn: suspend (exception: ManagedException)->R){
      handlers.forEach {
         registeredHandlers[it] = handlerFn
         notifyHandlerSet(it)
      }
   }

   suspend fun isHandlerPresent(handler: HandlerType): Boolean{
       return  registeredHandlers[handler]?.let { true }?:false
   }

   override suspend fun handleManaged(managedEx: ManagedException): HandlerResult<R> {
      val handlerFn = registeredHandlers[managedEx.handler]
      val handlingResult = handlerFn?.let {
           notifyHandled(managedEx)
           HandlerResult<R>(handlerFn.invoke(managedEx))
        } ?:run {
            notifyUnhandled(managedEx)
            HandlerResult<R>(null, managedEx)
         }
         return handlingResult
   }
}