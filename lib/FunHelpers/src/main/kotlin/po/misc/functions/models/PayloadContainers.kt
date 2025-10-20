package po.misc.functions.models

import po.misc.context.tracable.TraceableContext
import po.misc.functions.payloads.DoublePayload
import po.misc.functions.payloads.SafePayload


class DoublePayloadContainers<V1: Any, V2: Any>(
    override val value1: V1,
    override val value2: V2
) : DoublePayload<V1, V2> {


}

class SafePayloadContainers<V: Any>(
    override val host: TraceableContext,
    override val value: V?,
    override val throwable: Throwable?,
    ) : SafePayload<V> {

}