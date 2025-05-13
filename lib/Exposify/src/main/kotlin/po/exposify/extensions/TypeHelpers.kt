package po.exposify.extensions

import po.exposify.exceptions.InitException
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.misc.exceptions.ManagedException
import po.misc.exceptions.getOrException
import po.misc.types.castOrThrow
import po.misc.types.getOrThrow


inline fun <reified T: Any> Any?.castOrOperationsEx(
    message: String = "",
    code:  ExceptionCode = ExceptionCode.CAST_FAILURE): T
{
   return this.castOrThrow<T, OperationsException>(message, code.value)
}

inline fun <reified T: Any> Any?.castOrInitEx(
    message: String = "",
    code:  ExceptionCode = ExceptionCode.CAST_FAILURE): T
{
    return this.castOrThrow<T, InitException>(message, code.value)
}

inline fun <reified T: Any> Any.castLetOrInitEx(
    message: String = "",
    code:  ExceptionCode = ExceptionCode.CAST_FAILURE,
    block: (T)->T): T
{
    try {
       val result =  this.castOrThrow<T, InitException>(message, code.value)
       return block.invoke(result)
    }catch (ex: Throwable){
        throw  ex
    }
}


inline fun <reified T : Any> T?.getOrOperationsEx(
    message: String? = null,
    code: ExceptionCode = ExceptionCode.VALUE_IS_NULL
): T {
   return this.getOrThrow<T, OperationsException>(message, code.value)
}


@JvmName("getOrOperationsExNonReified")
fun <T : Any> T?.getOrOperationsEx(
    message: String,
    code: ExceptionCode = ExceptionCode.VALUE_IS_NULL
): T {

    if(this == null){
        throw OperationsException(message, code)
    }else{
        return this
    }

}

inline fun <reified T : Any> T?.getOrInitEx(
    message: String? = null,
    code: ExceptionCode = ExceptionCode.VALUE_IS_NULL
): T {
    return this.getOrThrow<T, InitException>(message, code.value)
}

@JvmName("getOrInitExNonReified")
fun <T : Any> T?.getOrInitEx(
    message: String,
    code: ExceptionCode = ExceptionCode.VALUE_IS_NULL
): T {
    if(this == null){
        throw InitException(message, code)
    }else{
        return this
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
