package po.misc.callbacks.events

import po.misc.exceptions.ManagedException
import po.misc.context.TraceableContext
import po.misc.types.TypeData
import po.misc.types.helpers.simpleOrNan
import po.misc.types.safeCast


interface Validatable

interface  ValidatableEvent{
    fun validate(value : Validatable, validationThrows: Boolean = false): Boolean
}

abstract class EventValidator<T:Validatable>(
    val typeData: TypeData<T>,
    val validatorFn: (T)-> Boolean
): ValidatableEvent, TraceableContext {

   override fun validate(value : Validatable, validationThrows: Boolean): Boolean{
       return value.safeCast<T>(typeData.kClass)?.let {
            validatorFn.invoke(it)
        }?:run {
            if(validationThrows){
                val expectTypeMsg = "Expecting : ${typeData.typeName}, received: ${value::class.simpleOrNan()}"
                throw ManagedException(this, "Wrong type passed for validation. $expectTypeMsg")
            }
            false
        }
   }
}
