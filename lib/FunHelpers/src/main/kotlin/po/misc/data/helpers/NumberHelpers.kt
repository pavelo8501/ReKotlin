package po.misc.data.helpers

import po.misc.exceptions.ManagedException


inline fun <R> String?.whenIsLong(action:(Long)->R):R?{
    if(this != null){
     return  toLongOrNull()?.let {
           action.invoke(it)
       }
    }
    return null
}

inline fun <R> String?.longOrManaged(action:(Long)->R):R{
    if(this != null){
        return  toLongOrNull()?.let {
            action.invoke(it)
        }?:run {
           throw ManagedException("Expected long but got $this")
        }
    }
    throw ManagedException("Expected long but got null")
}