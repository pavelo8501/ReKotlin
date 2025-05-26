package po.lognotify.extensions

import po.lognotify.exceptions.LoggerException
import po.misc.exceptions.HandlerType
import po.misc.types.castOrThrow

inline fun <reified T: Any> T?.getOrLoggerException(message: String):T{

    if(this != null){
        return this
    }else{
        val ex  = LoggerException(message)
        throw ex.setHandler(HandlerType.UNMANAGED)
    }
}

@PublishedApi
internal inline fun <reified T: Any> Any?.castOrLoggerException(message: String? = null): T {
  return  this.castOrThrow<T, LoggerException>(message)
}
