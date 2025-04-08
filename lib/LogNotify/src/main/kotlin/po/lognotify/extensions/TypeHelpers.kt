package po.lognotify.extensions

import po.lognotify.exceptions.CancellationException
import po.lognotify.exceptions.DefaultException
import po.lognotify.exceptions.ExceptionBase
import po.lognotify.exceptions.PropagatedException
import po.lognotify.exceptions.enums.CancelType
import po.lognotify.exceptions.enums.DefaultType
import po.lognotify.exceptions.enums.PropagateType
import kotlin.reflect.KClass

inline fun <reified T: Any> Any.safeCast(): T? {
    return this as? T
}

inline fun <T,  reified E: ExceptionBase> Iterable<T>.countEqualsOrThrow(equalsTo: Int, messageOnException: String = "", handlerValue : Int = 1):Iterable<T>{
    val actualCount = this.count()
    if(actualCount != equalsTo){
        val prefix = "CountEqualsFailed with test value $equalsTo actual is $actualCount"
        val exMessage = "$prefix. $messageOnException"
        val ex = when (E::class) {
            ExceptionBase.Default::class-> DefaultException(exMessage, DefaultType.fromValue(handlerValue))
            ExceptionBase.Propagate::class  -> PropagatedException(exMessage, PropagateType.fromValue(handlerValue))
            CancellationException::class -> CancellationException(exMessage, CancelType.fromValue(1))
            else -> DefaultException(exMessage, DefaultType.fromValue(handlerValue))
        }
       throw ex
    }else{
        return this
    }
}



fun <E: ExceptionBase>  E.throwCancel(msg: String= "", handler: CancelType = CancelType.CANCEL_ALL){
    CancellationException("${this.message} $msg", handler)
}

fun <E: ExceptionBase> E.throwPropagate(msg: String= "", handler: PropagateType = PropagateType.PROPAGATED){
    PropagatedException("${this.message} $msg", handler)
}


fun <T: Any> T?.getOrThrow(ex : ExceptionBase): T{
    return this ?: throw ex
}

fun Boolean.trueOrThrow(message: String): Boolean{
    if(!this){
       throw DefaultException("Tested value is false $message", DefaultType.DEFAULT)
    }
    return true
}

fun <T: Any> T?.getOrThrowDefault(message: String): T{
    val defaultException = DefaultException(message, DefaultType.DEFAULT)
    return this ?: throw defaultException
}

fun <T: Any> T?.getOrThrowCancellation(message: String, handlerType : CancelType = CancelType.SKIP_SELF): T{
    val defaultException = CancellationException(message, handlerType)
    return this ?: throw defaultException
}

fun <T: Any> T?.getOrThrowPropagate(message: String, handlerType : PropagateType = PropagateType.PROPAGATED): T{
    val defaultException = PropagatedException(message, handlerType)
    return this ?: throw defaultException
}

inline fun <T: Any> T?.letOrThrow(ex : ExceptionBase, block: (T)-> T): Unit{
    if(this != null){
        block(this)
    } else {
        throw ex
    }
}

inline fun <T: Any> T?.letOrThrow(message: String, code: Int,  processableBuilderFn: (String, Int) -> ExceptionBase, block: (T)-> T): T{
    return if (this != null) block(this) else throw processableBuilderFn.invoke(message, code)
}

