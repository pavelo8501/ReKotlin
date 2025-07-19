package po.exposify.exceptions

import po.exposify.exceptions.enums.ExceptionCode
import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedCallSitePayload
import po.misc.exceptions.text
import po.misc.context.Identifiable

fun Identifiable.managedPayload(
    message: String,
    source: ExceptionCode,
    handler: HandlerType? = HandlerType.SkipSelf,
    cause: Throwable? = null
):ManagedCallSitePayload{
    return ManagedCallSitePayload(this, message, source= source, handler = handler, cause = cause)
}

fun Identifiable.managedPayload(
    cause: Throwable,
    source: ExceptionCode,
    handler: HandlerType? = HandlerType.SkipSelf
):ManagedCallSitePayload{
    return ManagedCallSitePayload(this, cause.text(), handler = handler)
}