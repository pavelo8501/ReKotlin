package po.misc.functions

import po.misc.exceptions.ManagedCallSitePayload
import po.misc.interfaces.ObservedContext

interface ProbeContext<T>  {
    val exceptionPayload: ManagedCallSitePayload
}

