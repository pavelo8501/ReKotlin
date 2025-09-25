package po.lognotify.exceptions


import po.lognotify.common.LNInstance
import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException
import po.misc.context.CTX
import po.misc.exceptions.ManagedCallSitePayload
import po.misc.types.castOrThrow
import po.misc.types.helpers.simpleOrNan

class LoggerException(
    ctx: CTX,
    message: String,
    cause: Throwable? = null
) : ManagedException(ctx,  message, null, cause) {

    override var handler: HandlerType = HandlerType.CancelAll
}

@PublishedApi
internal inline fun <reified T: Any> T?.getOrLoggerException(lnInstance: LNInstance<*>, message: String):T{
    if(this != null){
        return this
    }else{
        val ex  = LoggerException(lnInstance, message)
        throw ex
    }
}

@PublishedApi
internal inline fun <reified T: Any> T?.getOrLoggerException(lnInstance: LNInstance<*>):T{
    if(this != null){
        return this
    }else{
        throw LoggerException(lnInstance.receiver, "Can not get ${T::class.simpleOrNan()} in ${lnInstance.identifiedByName}")
    }
}
