package po.managedtask.exceptions

import po.managedtask.classes.notification.NotifyTask
import po.managedtask.classes.task.TaskSealedBase
import po.managedtask.enums.SeverityLevel

interface ExceptionHandled {
   suspend fun setPropagatedExHandler(handlerFn: suspend (ex: ExceptionBase)-> Unit)
   suspend fun handlePropagated(ex: ExceptionBase.Propagate) : Boolean

   suspend fun setCancellationExHandler(handlerFn: suspend (ex: ExceptionBase)-> Unit)
   suspend fun handleCancellation(ex: ExceptionBase.Cancellation) : Boolean

   suspend fun setGenericExHandler(handlerFn: suspend (ex: Throwable)-> Unit)
   suspend fun handleGeneric(th: Throwable) : Boolean

   suspend fun subscribeHandlerUpdates(callback: suspend (notification: HandlerNotification) -> Unit)
}

data class HandlerNotification(
   val type: NotifyTask,
   val taskName: String,
   val nestingLevel: Int,
   val severity: SeverityLevel,
   val handlerName: String,
   val message : String,
   val handled: Boolean
)

class ExceptionHandler(
   private val task : TaskSealedBase<*>
) : ExceptionHandled {

   private var onExceptionThrown: (suspend (notification: HandlerNotification) -> Unit)? = null
   override suspend fun subscribeHandlerUpdates(callback: suspend (notification: HandlerNotification) -> Unit){
      onExceptionThrown = callback
   }

   private suspend fun notifyOnHandlerSet(handler : String){
      val msg = "$handler handle set"
      val notification = HandlerNotification(
         NotifyTask.HANDLER_SET,
         task.taskName,
         task.key.nestingLevel,
         SeverityLevel.INFO,
         handler,
         msg,
         true)
      onExceptionThrown?.invoke(notification)
   }

   private suspend fun notifyOnHandled(th : Throwable, handled: Boolean){
      var handlerName = "Generic(Unmanaged)"
      if(th is ExceptionBase){
         handlerName = th.handler.toString()
      }
      var severity = SeverityLevel.WARNING
      var notifyTask = NotifyTask.EXCEPTION_HANDLED
      val msg = if(handled){
         "Exception Handled. Exception Message : ${th.message.toString()}"
      }else{
         severity = SeverityLevel.EXCEPTION
         notifyTask = NotifyTask.EXCEPTION_UNHANDLED
         "Unhandled  : ${th.message.toString()}"
      }

      val notification = HandlerNotification(
         notifyTask,
         task.taskName,
         task.key.nestingLevel,
         severity,
         handlerName,
         msg,
         handled)
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