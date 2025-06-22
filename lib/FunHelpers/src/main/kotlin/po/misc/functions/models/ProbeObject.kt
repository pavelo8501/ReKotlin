package po.misc.functions.models

import po.misc.exceptions.ManagedCallsitePayload
import po.misc.exceptions.ManagedException
import po.misc.exceptions.exceptionPayload
import po.misc.functions.ProbeContext
import po.misc.interfaces.ObservedContext

class ProbeObject<T>(
    val receiver:T,
): ProbeContext<T>  where T : ObservedContext {


    override var exceptionPayload: ManagedCallsitePayload = ManagedCallsitePayload(receiver, "")

    fun provideExPayload(payload: ManagedCallsitePayload){
        exceptionPayload = payload
    }
}