package po.lognotify.extensions

import po.lognotify.exceptions.CancellationException
import po.lognotify.exceptions.DefaultException
import po.lognotify.exceptions.ExceptionBase
import po.lognotify.exceptions.enums.HandlerType

inline fun <reified T: Any> Any.safeCast(): T? {
    return this as? T
}

inline fun <T,  reified E: ExceptionBase> Iterable<T>.countEqualsOrThrow(equalsTo: Int, messageOnException: String = "", handlerValue : Int = 1):Iterable<T>{
    val actualCount = this.count()
    if(actualCount != equalsTo){
        val prefix = "CountEqualsFailed with test value $equalsTo actual is $actualCount"
        val exMessage = "$prefix. $messageOnException"
        val ex = when (E::class) {
           DefaultException::class-> DefaultException(exMessage, HandlerType.fromValue(handlerValue))
           CancellationException::class -> CancellationException(exMessage, HandlerType.fromValue(1))
            else -> DefaultException(exMessage, HandlerType.fromValue(handlerValue))
        }
       throw ex
    }else{
        return this
    }
}

fun <E: ExceptionBase>  E.throwCancel(msg: String= "", handler: HandlerType = HandlerType.CANCEL_ALL){
    CancellationException("${this.message} $msg", handler)
}

fun <T: Any> T?.getOrThrow(ex : ExceptionBase): T{
    return this ?: throw ex
}

fun Boolean.trueOrThrow(message: String): Boolean{
    if(!this){
       throw DefaultException("Tested value is false $message", HandlerType.SKIP_SELF)
    }
    return true
}

fun <T: Any> T?.getOrThrowDefault(message: String): T{
    val defaultException = DefaultException(message, HandlerType.SKIP_SELF)
    return this ?: throw defaultException
}

fun <T: Any> T?.getOrThrowCancellation(message: String, handlerType : HandlerType = HandlerType.SKIP_SELF): T{
    val defaultException = CancellationException(message, handlerType)
    return this ?: throw defaultException
}

inline fun <T: Any> T?.letOrThrow(ex : ExceptionBase, block: (T)-> T){
    if(this != null){
        block(this)
    } else {
        throw ex
    }
}

inline fun <T: Any> T?.letOrThrow(
    message: String,
    code: Int,
    processableBuilderFn: (String, Int) -> ExceptionBase, block: (T)-> T): T
{
    return if (this != null) block(this) else throw processableBuilderFn.invoke(message, code)
}

