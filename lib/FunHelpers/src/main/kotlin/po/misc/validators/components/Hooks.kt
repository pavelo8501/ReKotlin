package po.misc.validators.components

import po.misc.validators.SequentialContainer
import po.misc.validators.models.CheckStatus


sealed class  ValidatorHooks<T: Any?>(){

    internal var validationSuccess: ((CheckStatus)-> Unit)? = null
    internal var validationFailure: ((CheckStatus)-> Unit)? = null

    fun onValidationSuccess(success:(CheckStatus)-> Unit){
        validationSuccess = success
    }
    fun onValidationFailure(failure:(CheckStatus)-> Unit){
        validationFailure = failure
    }
}

class BaseHooks<T: Any?>(): ValidatorHooks<T>(){

}

class SequentialHooks<T: Any, R: Any?>(): ValidatorHooks<T>(){

    internal var conditionSuccess: ((R)-> Unit)? = null
    internal var conditionFailure: ((T)-> Unit)? = null
    internal var failure: ((Throwable, T)-> Unit)? = null

    internal var onResultCallback: ((CheckStatus)-> Unit)? = null

    fun onConditionSuccess(success:(R)-> Unit){
        conditionSuccess = success
    }
    fun onConditionFailure(failure:(T)-> Unit){
        conditionFailure = failure
    }
    fun onFault(error:(Throwable, T)-> Unit){
        failure = error
    }


    fun onResult(hook:(CheckStatus)-> Unit){
        onResultCallback = hook
    }
}

fun <T: Any, R: Any?>  SequentialContainer<T, R>.validatorHooks(
    block: SequentialHooks<T, R>.()-> Unit
):SequentialContainer<T, R>{
    block.invoke(sequentialHooks)
    return this
}
