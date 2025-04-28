package po.misc.types

import po.misc.exceptions.ManagedException
import po.misc.exceptions.SelfThrownException


inline fun <reified T: Any> Any.safeCast(): T? {
    return this as? T
}


inline fun <reified T: Any, reified E: ManagedException> Any?.castOrThrow(
    message: String = "",
    code: Int = 0
): T {

    if(this == null){
        throw SelfThrownException.build<E>("Unable to cast null to ${T::class.simpleName}", code)
    }else{
        val result =  this as? T
        if(result != null){
            return result
        }else{
            throw SelfThrownException.build<E>("Unable to cast ${this::class.simpleName} to  ${T::class.simpleName}", code)
        }
    }
}

