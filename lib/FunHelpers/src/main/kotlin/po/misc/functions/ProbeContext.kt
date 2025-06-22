package po.misc.functions

import po.misc.exceptions.ManagedCallsitePayload
import po.misc.interfaces.ObservedContext

interface ProbeContext<T>  {

    val exceptionPayload: ManagedCallsitePayload


}

