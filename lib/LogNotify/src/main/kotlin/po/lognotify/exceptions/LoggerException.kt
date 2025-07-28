package po.lognotify.exceptions


import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException
import po.misc.exceptions.ManageableException
import po.misc.context.CTX
import po.misc.exceptions.ManagedCallSitePayload
import po.misc.types.castOrThrow

class LoggerException(
    message: String,
    original: Throwable? = null
) : ManagedException(message, null, original) {

    constructor(payload: ManagedCallSitePayload): this(message = payload.message, original = payload.cause){
        initFromPayload(payload)
    }

    override var handler: HandlerType = HandlerType.CancelAll

    companion object : ManageableException.Builder<LoggerException> {
        override fun build(message: String, source: Enum<*>?, original: Throwable?): LoggerException {
            return LoggerException(message, original)
        }
    }
}

inline fun <reified T: Any>  Any?.castOrLoggerEx(ctx: CTX):T{
   return this.castOrThrow<T>(ctx){payload->
       LoggerException(payload)
   }
}
