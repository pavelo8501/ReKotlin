package po.lognotify.exceptions

import po.lognotify.classes.notification.enums.EventType
import po.lognotify.classes.notification.models.Notification
import po.lognotify.classes.notification.sealed.ProviderHandler
import po.lognotify.classes.task.TaskSealedBase
import po.lognotify.enums.SeverityLevel

interface ExceptionHandled {
   suspend fun setCancellationExHandler(handlerFn: suspend (ex: ExceptionBase)-> Unit)
   suspend fun handleCancellation(ex: CancellationException) : Boolean
   suspend fun setGenericExHandler(handlerFn: suspend (ex: Throwable)-> Unit)
   suspend fun handleGeneric(th: Throwable) : Boolean
   suspend fun subscribeHandlerUpdates(callback: suspend (notification: Notification) -> Unit)
}


class ExceptionHandler(
   private val task : TaskSealedBase<*>
) : ExceptionHandled {

   private var onHandlerUpdate: (suspend (notification: Notification) -> Unit)? = null
   override suspend fun subscribeHandlerUpdates(callback: suspend (notification: Notification) -> Unit){
      onHandlerUpdate = callback
   }

   private suspend fun notifyOnHandlerSet(handler : String){
      val message = "$handler handle set"
      val notification = Notification(
         task,
         EventType.HANDLER_REGISTERED,
         SeverityLevel.INFO,
         message, ProviderHandler(task.taskName))
      onHandlerUpdate?.invoke(notification)
   }
   private suspend fun notifyOnHandled(th : Throwable){
      val message =  "Exception Handled. Exception Message : ${th.message.toString()}"
      val severity = SeverityLevel.WARNING

      val notification = Notification(
         task,
         EventType.EXCEPTION_UNHANDLED,
         severity,
         message,
         ProviderHandler(task.taskName))
       onHandlerUpdate?.invoke(notification)
   }
   private suspend fun notifyOnUnHandled(th : Throwable){
      val severity = SeverityLevel.EXCEPTION
      val message =  "Unhandled  : ${th.message.toString()}"
      val notification = Notification(
         task,
         EventType.EXCEPTION_UNHANDLED,
         severity,
         message,
         ProviderHandler(task.taskName))
      onHandlerUpdate?.invoke(notification)
   }


   var cancelHandler: (suspend (ex: ExceptionBase) -> Unit)? = null
   override suspend fun setCancellationExHandler(handlerFn: suspend (ex: ExceptionBase) -> Unit) {
      cancelHandler = handlerFn
      notifyOnHandlerSet("CancellationExHandler")
   }

   override suspend fun handleCancellation(ex: CancellationException) : Boolean{
      if(cancelHandler != null){
         notifyOnHandled(ex)
         cancelHandler!!.invoke(ex)
         return true
      }else{
         notifyOnUnHandled(ex)
         return false
      }
   }

   var genericHandler: (suspend (ex: Throwable) -> Unit)? = null
   override suspend fun setGenericExHandler(handlerFn: suspend (ex: Throwable) -> Unit) {
      genericHandler = handlerFn
      notifyOnHandlerSet("GenericExHandler")
   }
   override suspend fun handleGeneric(th: Throwable) : Boolean {
      if(genericHandler != null){
         notifyOnHandled(th)
         genericHandler!!.invoke(th)
         return true
      }else{
         notifyOnUnHandled(th)
         return false
      }
   }
}