package po.misc.exceptions

import po.misc.interfaces.ObservedContext

fun ObservedContext.exceptionPayload(
    message: String,
    handlerType: HandlerType?,
    source: Enum<*>? = null,
    outputOverride:((ManagedException)-> Unit)? = null,
    builder: (ManagedCallsitePayload.()-> Unit)? = null
):ManagedCallsitePayload{
   val payload = ManagedCallsitePayload(this, message, handlerType, source)
   builder?.invoke(payload)
   return payload
}

fun exceptionOutput(callback: ()-> ManagedException):ManagedException{
    return  callback.invoke()
}