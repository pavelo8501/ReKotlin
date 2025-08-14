package po.lognotify.exceptions


import po.lognotify.common.LNInstance
import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException
import po.misc.context.CTX
import po.misc.exceptions.ManagedCallSitePayload
import po.misc.types.castOrThrow
import po.misc.types.helpers.simpleOrNan

class LoggerException(
    message: String,
    original: Throwable? = null
) : ManagedException(message, null, original) {

    constructor(payload: ManagedCallSitePayload): this(message = payload.message, original = payload.cause){
        initFromPayload(payload)
    }
    override var handler: HandlerType = HandlerType.CancelAll
}

@PublishedApi
internal inline fun <reified T: Any> T?.getOrLoggerException(message: String):T{
    if(this != null){
        return this
    }else{
        val ex  = LoggerException(message)
        throw ex
    }
}

@PublishedApi
internal inline fun <reified T: Any> T?.getOrLoggerException(lnInstance: LNInstance<*>):T{
    if(this != null){
        return this
    }else{
        throw LoggerException("Can not get ${T::class.simpleOrNan()} in ${lnInstance.identifiedByName}")
    }
}
