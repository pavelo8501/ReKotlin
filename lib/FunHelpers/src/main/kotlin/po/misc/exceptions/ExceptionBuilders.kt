package po.misc.exceptions

import po.misc.interfaces.IdentifiableContext
import po.misc.interfaces.ObservedContext

fun ObservedContext.exceptionPayload(
    message: String,
    handlerType: HandlerType?,
    source: Enum<*>? = null,
    outputOverride:((ManagedException)-> Unit)? = null,
    builder: (ManagedCallSitePayload.()-> Unit)? = null
):ManagedCallSitePayload{
   val payload = ManagedCallSitePayload(this, message, handlerType, source)
   builder?.invoke(payload)
   return payload
}

fun exceptionOutput(callback: ()-> ManagedException):ManagedException{
    return  callback.invoke()
}



//fun IdentifiableContext.managedPayload(
//    message: String? = null,
//    handlerType: HandlerType = HandlerType.SkipSelf,
//    source: Enum<*>? = null,
//    outputOverride:((ManagedException)-> Unit)? = null
//):ManagedCallSitePayload{
//    return ManagedCallSitePayload(this, message?:"", handlerType, source)
//}