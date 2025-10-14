package po.misc.functions.common

import po.misc.exceptions.ThrowableCallSitePayload
import po.misc.exceptions.ManagedPayload


interface Fallback<T: Any>{
    fun provideValue(newValue:T)
    fun initiateFallback():T
}


class ValueFallback<T: Any>(
   val valueFallback:()-> T
): Fallback<T>{

    private var value:T? = null

    override fun provideValue(newValue:T){
        value = newValue
    }

    override fun initiateFallback():T{
        return value?:run {
            valueFallback.invoke()
        }
    }
}

class ExceptionFallback(
   val  exceptionProvider:()-> Throwable
): Fallback<Nothing>{

    override fun provideValue(newValue:Nothing){

    }

    override fun initiateFallback(): Nothing{
        throw exceptionProvider.invoke()
    }
}