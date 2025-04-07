package po.lognotify.exceptions

import po.lognotify.classes.notification.enums.EventType
import po.lognotify.classes.notification.enums.InfoProvider
import po.lognotify.classes.notification.models.Notification
import po.lognotify.classes.task.TaskSealedBase
import po.lognotify.enums.SeverityLevel

interface ExceptionHandled {
   suspend fun setPropagatedExHandler(handlerFn: suspend (ex: ExceptionBase)-> Unit)
   suspend fun handlePropagated(ex: ExceptionBase.Propagate) : Boolean

   suspend fun setCancellationExHandler(handlerFn: suspend (ex: ExceptionBase)-> Unit)
   suspend fun handleCancellation(ex: ExceptionBase.Cancellation) : Boolean

   suspend fun setGenericExHandler(handlerFn: suspend (ex: Throwable)-> Unit)
   suspend fun handleGeneric(th: Throwable) : Boolean

   suspend fun subscribeHandlerUpdates(callback: suspend (notification: Notification) -> Unit)
}


class ExceptionHandler(
   private val task : TaskSealedBase<*>
) : ExceptionHandled {

   private var onExceptionThrown: (suspend (notification: Notification) -> Unit)? = null
   override suspend fun subscribeHandlerUpdates(callback: suspend (notification: Notification) -> Unit){
      onExceptionThrown = callback
   }

   private suspend fun notifyOnHandlerSet(handler : String){

      val message = "$handler handle set"
      val notification = Notification(
         task.taskName,
         task.nestingLevel,
         EventType.HANDLER_REGISTERED,
         SeverityLevel.INFO,
         message, InfoProvider.EX_HANDLER)
      onExceptionThrown?.invoke(notification)
   }

   private suspend fun notifyOnHandled(th : Throwable, handled: Boolean){
      var handlerName = "Generic(Unmanaged)"
      if(th is ExceptionBase){
         handlerName = th.handler.toString()
      }
      var eventType  =  EventType.EXCEPTION_UNHANDLED
      var severity = SeverityLevel.WARNING
      val msg = if(handled){
         eventType = EventType.EXCEPTION_HANDLED
         "Exception Handled. Exception Message : ${th.message.toString()}"
      }else{
         severity = SeverityLevel.EXCEPTION
         "Unhandled  : ${th.message.toString()}"
      }

      val notification = Notification(
         task.taskName,
         task.nestingLevel,
         eventType,
         severity,
         msg,
         InfoProvider.EX_HANDLER)
      onExceptionThrown?.invoke(notification)


      onExceptionThrown?.invoke(notification)
   }

   private var propagatedHandler: (suspend (ex: ExceptionBase) -> Unit)? = null
   override suspend fun setPropagatedExHandler(handlerFn: suspend (ex: ExceptionBase) -> Unit) {
      propagatedHandler = handlerFn
      notifyOnHandlerSet("PropagatedExHandler")
   }
   override suspend fun handlePropagated(ex: ExceptionBase.Propagate): Boolean {
      if(propagatedHandler != null){
         notifyOnHandled(ex, true)
         propagatedHandler!!.invoke(ex)
         return true
      }else{
         notifyOnHandled(ex, false)
        return false
      }
   }

   var cancelHandler: (suspend (ex: ExceptionBase) -> Unit)? = null
   override suspend fun setCancellationExHandler(handlerFn: suspend (ex: ExceptionBase) -> Unit) {
      cancelHandler = handlerFn
      notifyOnHandlerSet("CancellationExHandler")
   }

   override suspend fun handleCancellation(ex: ExceptionBase.Cancellation) : Boolean{
      if(!ex.invokeCancellation()){
         if(cancelHandler != null){
            notifyOnHandled(ex, true)
            propagatedHandler!!.invoke(ex)
            return true
         }else{
            notifyOnHandled(ex, false)
            return false
         }
      }else{
         notifyOnHandled(ex, true)
         return true
      }
   }

   var genericHandler: (suspend (ex: Throwable) -> Unit)? = null
   override suspend fun setGenericExHandler(handlerFn: suspend (ex: Throwable) -> Unit) {
      genericHandler = handlerFn
      notifyOnHandlerSet("GenericExHandler")
   }
   override suspend fun handleGeneric(th: Throwable) : Boolean {
      if(genericHandler != null){
         notifyOnHandled(th, true)
         genericHandler!!.invoke(th)
         return true
      }else{
         notifyOnHandled(th, false)
         return false
      }
   }
}