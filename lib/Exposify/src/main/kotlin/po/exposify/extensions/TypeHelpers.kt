package po.exposify.extensions

import po.exposify.exceptions.ExceptionCodes
import po.exposify.exceptions.OperationsException
import po.lognotify.eventhandler.exceptions.ProcessableException
import po.lognotify.shared.enums.HandleType
import kotlin.reflect.KClass

inline fun <reified T: Any> Any.safeCast(): T? {
    return this as? T
}


fun <T: Any> T?.getOrThrow(ex : ProcessableException): T{
    return this ?: throw ex
}

inline fun <T: Any> T?.getOrThrow(message: String, code: ExceptionCodes,  processableBuilderFn: (String, Int) -> ProcessableException): T{
    return this ?: run {
      throw processableBuilderFn.invoke(message, code.value)
    }
}

inline fun <T: Any> T?.letOrThrow(ex : OperationsException, block: (T)-> T): Unit{
    if(this != null){
        block(this)
    } else {
        throw ex
    }
}

inline fun <T: Any> T?.letOrThrow(message: String, code: ExceptionCodes,  processableBuilderFn: (String, Int) -> ProcessableException, block: (T)-> T): T{
    return if (this != null) block(this) else throw processableBuilderFn.invoke(message, code.value)
}

inline fun <T: Iterable<Any>> T.countEqualsOrWithThrow(equalsTo: Int, block:  (processableBuilderFn: ((message : String, code : ExceptionCodes)->OperationsException))-> OperationsException):T{
    if(this.count() != equalsTo){
        val default :  (message : String, code : ExceptionCodes)-> OperationsException = {message, code->  OperationsException(message, code) }
        val composedException  =  block(default)
        throw composedException as Throwable
    }else{
        return this
    }
}



//inline fun <T: Any, R: Any> T?.getOrThrow(ex : ProcessableException, block: (T) -> R): R{
//    return if (this != null) block(this) else throw ex
//}


inline fun <T1 : Any, R : Any> safeLet(p1: T1?, block: (T1) -> R?): R? {
    return if (p1 != null) block(p1) else null
}


inline fun <reified T: Any> getType(): KClass<T> {
    return T::class
}

inline fun <reified T, reified U> initializeContexts(
    receiverInstance: T,
    paramInstance: U,
    block: T.(U) -> Unit
) {
    receiverInstance.block(paramInstance)
}