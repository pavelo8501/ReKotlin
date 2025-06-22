package po.lognotify.exceptions


import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException
import po.misc.exceptions.ManageableException
import po.misc.types.castOrThrow

class LoggerException(
    message: String,
    original: Throwable? = null
) : ManagedException(message, null, original) {

    override var handler: HandlerType = HandlerType.UNMANAGED

    companion object : ManageableException.Builder<LoggerException> {
        override fun build(message: String, source: Enum<*>?, original: Throwable?): LoggerException {
            return LoggerException(message, original)
        }
    }
}

inline fun <reified T: Any>  Any?.castOrLoggerEx(where: String):T{
   return this.castOrThrow<T,LoggerException>(where)
}
