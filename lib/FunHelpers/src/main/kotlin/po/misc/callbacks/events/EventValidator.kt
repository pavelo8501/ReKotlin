package po.misc.callbacks.events

import po.misc.exceptions.ManagedException
import po.misc.context.tracable.TraceableContext
import po.misc.exceptions.handling.Suspended
import po.misc.types.helpers.simpleOrAnon
import po.misc.types.safeCast
import po.misc.types.token.TypeToken


interface Validatable

interface  ValidatableEvent{

    fun validate(value : Validatable, validationThrows: Boolean = false): Boolean
}

abstract class EventValidator<T:Validatable>(
    val typeData: TypeToken<T>,
    val validatorFn: (T)-> Boolean
): ValidatableEvent, TraceableContext {

   override fun validate(value : Validatable, validationThrows: Boolean): Boolean{
       return value.safeCast<T>(typeData.kClass)?.let {
            validatorFn.invoke(it)
        }?:run {
            if(validationThrows){
                val expectTypeMsg = "Expecting : ${typeData}, received: ${value::class.simpleOrAnon}"
                throw ManagedException(this, "Wrong type passed for validation. $expectTypeMsg")
            }
            false
        }
   }
}



class Validator<H: EventHost, T: Any, R: Any >(
    private val hostedEvent: HostedEvent<H, T, R>,
    var validatorFn: ((T)-> Boolean)?
): TraceableContext {

    internal  var result: Boolean? = null

    internal var onValidationFailure: (H.(T)->R)? = null
    internal var onValidationFailureSuspending: (suspend H.(T)->R)? = null

    constructor(hostedEvent: HostedEvent<H, T, R>):this(hostedEvent, null)

    fun validate(value: T): R?{
      val validationLambda = validatorFn
      if(validationLambda  ==  null){
          return null
      }else{
          result = validationLambda.invoke(value)
          return if(result?:false){
              hostedEvent.trigger(value)
          }else{
              onValidationFailure?.invoke(hostedEvent.host, value) ?:run {
                  null
              }
          }
      }
    }

    fun registerValidator(validator:(T)-> Boolean){
        validatorFn = validator
    }

    suspend fun validate(value: T, suspending: Suspended): R?{
        val validationLambda = validatorFn
        if(validationLambda  ==  null){
            return null
        }else{
            result = validationLambda.invoke(value)
            return if(result?:false){
                hostedEvent.trigger(value, suspending)
            }else{
                onValidationFailureSuspending?.invoke(hostedEvent.host, value) ?:run {
                    null
                }
            }
        }
    }
    fun onValidationFailure(callback:H.(T) -> R){
        onValidationFailure = callback
    }
    fun onValidationFailure(suspended: Suspended, callback:suspend H.(T) -> R){
       onValidationFailureSuspending = callback
    }

    fun onValidationSuccess(suspended: Suspended, callback: suspend H.(T) -> R?) = hostedEvent.onEvent(suspended, callback)
    fun onValidationSuccess(callback:H.(T) -> R) = hostedEvent.onEvent(callback)

}


