package po.misc.callbacks.validator

import po.misc.exceptions.handling.Suspended



class ReactiveValidator<T: Any>(
    val predicate: (T)-> Boolean
): ValidationProvider<T>{

    private var onSuccess: ((T)-> Unit)? = null
    private var onSuccessSuspending :  (suspend (T)-> Unit)? = null
    private var onFailure: ((T)-> Unit)? = null
    private var onFailureSuspending: (suspend (T)-> Unit)? = null

    override fun validate(data: T): Boolean{
        val result = predicate(data)
        if(result){
            onSuccess?.invoke(data)
        }else{
            onFailure?.invoke(data)
        }
        return result
    }

    suspend fun validate(data: T, suspending: Suspended): Boolean{
        val result = predicate(data)
        if(result){
            onSuccessSuspending?.invoke(data)
        }else{
            onFailureSuspending?.invoke(data)
        }
        return result
    }

    fun onSuccess(block: (T)-> Unit){
        onSuccess = block
    }
    fun onSuccess(suspending: Suspended, block:suspend (T)-> Unit){
        onSuccessSuspending = block
    }

    fun onFailure(block: (T)-> Unit){
        onFailure = block
    }
    fun onFailure(suspending: Suspended, block:suspend (T)-> Unit){
        onFailureSuspending = block
    }

}