package po.lognotify.extensions

import po.lognotify.exceptions.CancellationException
import po.lognotify.exceptions.DefaultException
import po.lognotify.exceptions.ExceptionBase
import po.lognotify.exceptions.PropagatedException
import po.lognotify.exceptions.enums.CancelType
import po.lognotify.exceptions.enums.DefaultType
import po.lognotify.exceptions.enums.PropagateType


inline fun <reified T: Any> Any.safeCast(): T? {
    return this as? T
}

fun <T: Any> T?.getOrThrow(ex : ExceptionBase): T{
    return this ?: throw ex
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

