package po.lognotify.extensions

import po.lognotify.exceptions.LoggerException
import po.lognotify.exceptions.enums.HandlerType

internal fun <T: Any> T?.getOrThrow(message: String):T{
   return getOrException(LoggerException(message, HandlerType.UNMANAGED))
}

@PublishedApi
internal inline fun <reified T: Any> Any.castOrThrow(message: String): T {
    return castOrException<T, LoggerException>(LoggerException(message, HandlerType.UNMANAGED))
}
