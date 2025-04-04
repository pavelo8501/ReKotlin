package po.managedtask.exceptions

enum class ManagedHandleType {
    DEFAULT,
    SKIP_SELF,
    CANCEL_ALL,
    PROPAGATED,
}

interface CanBeThrown {

    val managedExceptionBase get() = (this as ManagedExceptionBase)

    fun throwThis(message: String) {
        managedExceptionBase.message = message
        throw managedExceptionBase
    }
}

class DefaultException(message: String, errorCode : Int = 0) : ManagedExceptionBase(message, ManagedHandleType.DEFAULT)
class SkipException(message: String, errorCode : Int = 0) : ManagedExceptionBase(message, ManagedHandleType.SKIP_SELF)
class CancellationException(message: String, errorCode : Int = 0
) : ManagedExceptionBase(message, ManagedHandleType.CANCEL_ALL)
{

}

class PropagatedException(message: String, errorCode : Int = 0) : ManagedExceptionBase(message, ManagedHandleType.PROPAGATED)


abstract class ManagedExceptionBase(
    override var message: String,
    var handleType: ManagedHandleType = ManagedHandleType.DEFAULT,
    val errorCode : Int = 0
) : Throwable(message), CanBeThrown{

    private var cancellationFn: ((ManagedExceptionBase) -> Unit)? = null

    fun setCancellationHandler(handlerFn: (ManagedExceptionBase) -> Unit){
        cancellationFn = handlerFn
    }
    fun invokeCancellation(): Boolean{
        if(cancellationFn != null){
            cancellationFn!!.invoke(this)
            return true
        }else{
            return false
        }
    }

    private var source : Throwable? = null
    fun setSourceException(th: Throwable){
        source = th
    }

}