package po.lognotify.extensions

import po.lognotify.exceptions.LoggerException
import po.misc.exceptions.HandlerType
import po.misc.exceptions.castOrException

internal inline fun <reified T: Any> T?.getOrLoggerException(message: String):T{

    if(this != null){
        return this
    }else{
        throw LoggerException(message, HandlerType.UNMANAGED)
    }
}

@PublishedApi
internal inline fun <reified T: Any> Any?.castOrLoggerException(): T {

   return this.castOrException<T> {
       LoggerException("", HandlerType.UNMANAGED)
    }
}
