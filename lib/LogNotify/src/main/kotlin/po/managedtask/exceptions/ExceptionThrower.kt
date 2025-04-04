package po.managedtask.exceptions

class ExceptionThrower : ExceptionsThrown {

    override fun throwDefaultException(message : String){
        DefaultException(message)
    }

    override fun throwDefaultException(ex : Throwable){
        val def =  DefaultException(ex.message.toString())
        def.setSourceException(ex)
        throw def
    }

    override fun throwSkipException(message : String){
        SkipException(message)
    }

    override fun throwCancellationException(
        message: String,
        cancelHandler: ((ManagedExceptionBase) -> Unit)?
    ) {
        val ex = CancellationException(message)
        if(cancelHandler!=null){
            ex.setCancellationHandler(cancelHandler)
        }
        throw  ex
    }

    override fun throwPropagatedException(message : String){
        throw PropagatedException(message)
    }

}