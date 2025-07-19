package po.misc.functions

import po.misc.exceptions.ManagedCallSitePayload

interface ProbeContext<T>  {
    val exceptionPayload: ManagedCallSitePayload
}

