package po.exposify.extensions

import po.exposify.exceptions.InitException
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.lognotify.exceptions.ManagedException
import po.lognotify.exceptions.enums.HandlerType
import po.lognotify.extensions.castOrException
import po.lognotify.extensions.getOrException
import kotlin.reflect.KClass


inline fun <reified T: Any> Any.safeCast(): T? {
    return this as? T
}

inline fun <reified T: Any> Any.castOrOperationsEx(
    message: String,
    code:  ExceptionCode,
    handlerType : HandlerType = HandlerType.CANCEL_ALL): T
{
    return  this.castOrException(OperationsException(message, code, handlerType))
}

fun <T: Any> T?.getOrInitEx(
    message: String,
    code:  ExceptionCode,
    handlerType : HandlerType = HandlerType.CANCEL_ALL): T{
    return  this.getOrException(InitException(message, code, handlerType))
}

fun <T: Any> T?.getOrOperationsEx(
    message: String,
    code:  ExceptionCode,
    handlerType : HandlerType = HandlerType.SKIP_SELF): T{
    return  this.getOrException(OperationsException(message, code, handlerType))
}

inline fun <T: Any> T?.letOrException(ex : ManagedException, block: (T)-> T){
    if(this != null){
        block(this)
    } else {
        throw ex
    }
}


fun <T: Any?, E: ManagedException> T.testOrOperationsEx(exception : E, predicate: (T) -> Boolean): T{
    if (predicate(this)){
        return this
    }else{
        throw exception
    }
}


inline fun <T: Any> T?.letOrThrow(ex : OperationsException, block: (T)-> T): Unit{
    if(this != null){
        block(this)
    } else {
        throw ex
    }
}
