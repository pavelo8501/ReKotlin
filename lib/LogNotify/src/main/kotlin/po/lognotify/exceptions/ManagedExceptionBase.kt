package po.lognotify.exceptions

import po.lognotify.exceptions.enums.HandlerType


interface SelfThrownException {

    fun throwDefault(message: String, handler : HandlerType = HandlerType.GENERIC, sourceTh: Throwable? = null): DefaultException{
       val newDefault =  DefaultException(message, handler)
        if(sourceTh !=null){
            newDefault.setSourceException(sourceTh)
        }
        throw newDefault
    }
    fun throwCancel(message: String, handler : HandlerType = HandlerType.SKIP_SELF, sourceTh: Throwable? = null): CancellationException{
        val newCancel =  CancellationException(message, handler)
        if(sourceTh !=null){
            newCancel.setSourceException(sourceTh)
        }
        throw newCancel
    }
}


class LoggerException(message: String) : ExceptionBase(message, HandlerType.UNMANAGED), SelfThrownException

open class DefaultException(
    message: String,
    override var handler: HandlerType,
    errorCode : Int = 0) :ExceptionBase(message, handler, errorCode)

open class CancellationException(
    message: String,
    override var handler  : HandlerType,
    errorCode : Int = 0) :ExceptionBase(message, handler, errorCode)

sealed class ExceptionBase(
    override val message: String,
    open var handler: HandlerType,
    errorCode : Int = 0
) : Throwable(message), SelfThrownException{


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