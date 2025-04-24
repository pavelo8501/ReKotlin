package po.misc.exceptions

import kotlin.reflect.full.companionObjectInstance


inline fun <T : Any> T?.getOrException(
    exceptionProvider: () -> ManagedException
): T {
    return this ?: throw exceptionProvider()
}


inline fun <reified T: Any, reified E: ManagedException> Any?.castOrThrow(
    message: String = "",
    handler: HandlerType = HandlerType.CANCEL_ALL
): T {

    if(this == null){
        throw SelfThrownException.build<E>("Unable to cast null to ${T::class.simpleName}", handler)
    }else{
        val result =  this as? T
        if(result != null){
            return result
        }else{
            throw SelfThrownException.build<E>("Unable to cast ${this::class.simpleName} to  ${T::class.simpleName}", handler)
        }
    }
}

inline fun <reified T: Any> Any?.castOrException(
    exceptionProvider: () -> ManagedException
): T {

    if(this == null){
       val exception =  exceptionProvider()
       throw exception.addMessage("Unable to cast null to ${T::class.simpleName}")

    }else{
        val result =  this as? T
        if(result != null){
            return result
        }else{
            val exception =  exceptionProvider()
            throw exception.addMessage("Unable to cast ${this::class.simpleName} to  ${T::class.simpleName}")
        }
    }
}


inline fun <T: Any> T?.letOrException(ex : ManagedException, block: (T)-> T){
    if(this != null){
        block(this)
    } else {
        throw ex
    }
}

fun <T: Any?, E: ManagedException> T.testOrException( exception : E, predicate: (T) -> Boolean): T{
    if (predicate(this)){
        return this
    }else{
        throw exception
    }
}

inline fun <reified T> Iterable<T>.countEqualsOrException(equalsTo: Int, exception:ManagedException):Iterable<T>{

    val actualCount = this.count()
    if(actualCount != equalsTo){
        throw exception
    }else{
        return this
    }
}