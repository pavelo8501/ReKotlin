package po.misc.functions.common

import po.misc.exceptions.ManagedCallSitePayload
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

class ExceptionFallback<T: Any>(
   val  exceptionProvider:(ManagedCallSitePayload)-> Throwable
): Fallback<T>{

    private var value:T? = null

    override fun provideValue(newValue:T){
        value = newValue
    }

    override fun initiateFallback(): T{
        return value?:run {
            throw exceptionProvider.invoke(ManagedPayload("value null", "initiateFallback", this))
        }
    }
}