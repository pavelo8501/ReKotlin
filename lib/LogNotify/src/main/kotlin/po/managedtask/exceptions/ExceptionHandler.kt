package po.managedtask.exceptions

interface ExceptionHandler {
   fun setPropagatedExHandler(handlerFn: (ex: ManagedExceptionBase)-> Unit)
   fun setCancellationExHandler(handlerFn: (ex: ManagedExceptionBase)-> Unit)
   fun setGenericExHandler(handlerFn: (ex: Throwable)-> Unit)

}

