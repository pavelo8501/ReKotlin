package po.misc.exceptions

import po.misc.context.Identifiable


//fun ObservedContext.exceptionPayload(
//    producer: Identifiable,
//    message: String,
//    handlerType: HandlerType?,
//    source: Enum<*>? = null,
//    builder: (ManagedCallSitePayload.()-> Unit)? = null
//):ManagedCallSitePayload{
//   val payload = ManagedCallSitePayload(producer, message, handlerType, source)
//   builder?.invoke(payload)
//   return payload
//}