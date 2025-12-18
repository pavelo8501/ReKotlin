package po.misc.callbacks.validator

import po.misc.functions.Suspended
import java.time.Instant


abstract class ValidityConditionBase<T>(
    val predicate: (T)-> Boolean
){

    abstract fun validated(parameter: T)
    abstract fun failed(parameter: T)

    fun validate(parameter: T): Boolean{
       val result = predicate.invoke(parameter)
       if(result){
           validated(parameter)
           return true
       }
       failed(parameter)
       return false
    }
}


class ValidityCondition<T>(
    val conditionName:String,
    predicate: (T)-> Boolean
): ValidityConditionBase<T>(predicate){

    data class ValidationResult<T>(val name:String, val parameter:T, val success : Boolean){
        val validatedAt : Instant = Instant.now()
    }

    private var onValidated: ((ValidationResult<T>)-> Unit)? = null
    private var onFailure: ((ValidationResult<T>)-> Unit)? = null

    override fun validated(parameter: T){
        onValidated?.invoke(ValidationResult(conditionName, parameter, success = true))
    }

    override fun failed(parameter: T) {
        onFailure?.invoke(ValidationResult(conditionName, parameter, success = false))
    }

    fun onValidated(callback: (ValidationResult<T>)-> Unit){
        onValidated = callback
    }

    fun onFailure(callback: (ValidationResult<T>)-> Unit){
        onFailure = callback
    }
}





class ReactiveValidator<T>(
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

    override suspend fun validate(data: T, suspending: Suspended): Boolean{
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