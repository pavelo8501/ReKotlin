package po.misc.data.helpers

import po.misc.exceptions.ManagedException
import po.misc.types.getOrThrow


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

inline fun <T: Any, R> T.longOrManaged(param1:String?,  action: T.(Long)->R):R{
    val param1Long = param1?.toLongOrNull().getOrThrow(Long::class, this){
        IllegalArgumentException("parameter1 expected to be long")
    }
    return action.invoke(this, param1Long)
}





inline fun <T: Any, R> T.longOrManaged(param1:String?, param2:String?,  action:T.(Long, Long)->R):R{
    val param1Long = param1?.toLongOrNull().getOrThrow(Long::class, this){
        IllegalArgumentException("parameter1 expected to be long")
    }
    val param2Long = param2?.toLongOrNull().getOrThrow(Long::class, this){
        IllegalArgumentException("parameter2 expected to be long")
    }

   return action.invoke(this, param1Long, param1Long)

}