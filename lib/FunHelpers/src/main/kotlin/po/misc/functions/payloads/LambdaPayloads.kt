package po.misc.functions.payloads

import po.misc.exceptions.ManagedException
import po.misc.context.tracable.TraceableContext


sealed interface LambdaPayload<T> {

}


interface SafePayload<V: Any>: LambdaPayload<V> {

    val value:V?
    val throwable: Throwable?
    val host: TraceableContext

    val isValue: Boolean get() = value != null
    val isThrowable: Boolean get() = throwable != null

    val resultOrThrow: V get() = value?: throw throwable?: ManagedException(host, "Both result and throwable are null")

}

interface DoublePayload<V1: Any, V2: Any>: LambdaPayload<V1> {

    val value1:V1
    val value2:V2

}




