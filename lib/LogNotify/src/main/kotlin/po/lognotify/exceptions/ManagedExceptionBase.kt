package po.lognotify.exceptions

import po.lognotify.exceptions.enums.CancelType
import po.lognotify.exceptions.enums.DefaultType
import po.lognotify.exceptions.enums.PropagateType


interface SelfThrownException {

    fun throwDefault(message: String, handler : DefaultType = DefaultType.DEFAULT, sourceTh: Throwable? = null): DefaultException{
       val newDefault =  DefaultException(message, handler)
        if(sourceTh !=null){
            newDefault.setSourceException(sourceTh)
        }
        throw newDefault
    }

    fun throwCancel(message: String, handler : CancelType = CancelType.SKIP_SELF, sourceTh: Throwable? = null): CancellationException{
        val newCancel =  CancellationException(message, handler)
        if(sourceTh !=null){
            newCancel.setSourceException(sourceTh)
        }
        throw newCancel
    }

    fun throwPropagate(message: String, handler : PropagateType = PropagateType.PROPAGATED, sourceTh: Throwable? = null): PropagatedException{
        val newDefault =  PropagatedException(message, handler)
        if(sourceTh !=null){
            newDefault.setSourceException(sourceTh)
        }
        throw newDefault
    }
}


class DefaultException(override var message: String, var handlerType: DefaultType) : ExceptionBase.Default(message, handlerType), SelfThrownException

class CancellationException(
    override var message: String,
    var handlerType : CancelType
) : ExceptionBase.Cancellation(message, handlerType), SelfThrownException

class PropagatedException(override var message: String, var handlerType : PropagateType) : ExceptionBase.Propagate(message,  handlerType), SelfThrownException

class Terminator(message: String) : ExceptionBase.Default(message, DefaultType.UNMANAGED), SelfThrownException

sealed class ExceptionBase(
    override val message: String,
    open var handler: Int  = 0,
    errorCode : Int = 0
) : Throwable(message), SelfThrownException{

    abstract class Default(
        message: String,
        handlerType: DefaultType,
        errorCode : Int = 0) :ExceptionBase(message, handlerType.value, errorCode)
    abstract class Cancellation(
        message: String,
        handlerType  : CancelType,
        errorCode : Int = 0) :ExceptionBase(message, handlerType.value, errorCode)
    abstract class Propagate(
        message: String,
        handlerType: PropagateType,
        errorCode : Int = 0) :ExceptionBase(message, handlerType.value, errorCode)


    private var cancellationFn: ((ExceptionBase) -> Unit)? = null
    fun setCancellationHandler(handlerFn: (ExceptionBase) -> Unit){
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
    fun setSourceException(th: Throwable): ExceptionBase{
        source = th
        return this
    }
    fun reThrowSource(){
        if(source !=null){
            throw source!!
        }else{
            throw Exception("Rethrow Source exception failed. No source set in containing ${handler.toString()} with message $message")
        }
    }
}