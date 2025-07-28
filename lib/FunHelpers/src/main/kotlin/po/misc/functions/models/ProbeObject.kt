package po.misc.functions.models

import po.misc.functions.ProbeContext
import po.misc.context.ObservedContext

class ProbeObject<T>(
    val receiver:T,
): ProbeContext<T>  where T : ObservedContext {


}