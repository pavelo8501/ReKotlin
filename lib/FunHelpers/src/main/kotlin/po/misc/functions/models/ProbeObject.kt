package po.misc.functions.models

import po.misc.functions.ProbeContext
import po.misc.context.ObservedContext
import po.misc.exceptions.ExceptionPayload

class ProbeObject<T>(
    val receiver:T,
): ProbeContext<T>  where T : ObservedContext {

    override var exceptionPayload: ExceptionPayload = ExceptionPayload(receiver, "")

    fun provideExPayload(payload: ExceptionPayload){
        exceptionPayload = payload
    }
}