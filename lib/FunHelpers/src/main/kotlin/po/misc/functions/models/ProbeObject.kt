package po.misc.functions.models

import po.misc.exceptions.ManagedCallSitePayload
import po.misc.exceptions.ManagedException
import po.misc.exceptions.exceptionPayload
import po.misc.functions.ProbeContext
import po.misc.interfaces.ObservedContext

class ProbeObject<T>(
    val receiver:T,
): ProbeContext<T>  where T : ObservedContext {


    override var exceptionPayload: ManagedCallSitePayload = ManagedCallSitePayload(receiver, "")

    fun provideExPayload(payload: ManagedCallSitePayload){
        exceptionPayload = payload
    }
}