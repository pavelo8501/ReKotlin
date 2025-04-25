package po.exposify.extensions

import po.exposify.exceptions.InitException
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException
import po.misc.exceptions.castOrException
import po.misc.exceptions.getOrException

inline fun <reified T: Any> Any.safeCast(): T? {
    return this as? T
}

inline fun <reified T: Any> Any?.castOrOperationsEx(
    message: String = "",
    code:  ExceptionCode = ExceptionCode.CAST_FAILURE): T
{
    return  this.castOrException{
        OperationsException(message, code)
    }
}

inline fun <reified T: Any> Any?.castOrInitEx(
    message: String = "",
    code:  ExceptionCode = ExceptionCode.CAST_FAILURE): T
{
    return  this.castOrException{
        InitException(message, code)
    }
}

inline fun <reified T: Any> Any.castLetOrInitEx(
    message: String = "",
    code:  ExceptionCode = ExceptionCode.CAST_FAILURE,
    block: (T)->T): T
{
    try {
       val result =  castOrException<T>{
           InitException(message, code)
       }
       return block.invoke(result)
    }catch (ex: Throwable){
        throw  ex
    }
}



internal inline fun <reified T: Any> T?.getOrInitEx(
    message: String,
    code:  ExceptionCode): T{
    return  this.getOrException {

        InitException(message, code)
    }
}

//fun <T: Any> T?.getOrOperationsEx(
//    message: String  = "",
//    code:  ExceptionCode = ExceptionCode.VALUE_IS_NULL,
//    handlerType : HandlerType = HandlerType.SKIP_SELF): T{
//    return  this.getOrException(OperationsException(message, code, handlerType))
//}

fun <T : Any> T?.getOrOperationsEx(
    message: String = "Value is null",
    code: ExceptionCode = ExceptionCode.VALUE_IS_NULL
): T {
    return this.getOrException {
        OperationsException(message, code)
    }
}


fun <T: Any?, E: ManagedException> T.testOrThrow(exception : E, predicate: (T) -> Boolean): T{
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
