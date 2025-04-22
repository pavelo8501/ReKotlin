package po.lognotify.exceptions

import po.lognotify.exceptions.enums.HandlerType
import po.lognotify.extensions.getOrException


class LoggerException(
    message: String,
    handler: HandlerType = HandlerType.UNMANAGED
) : ManagedException(message, handler) {

    override val builderFn: (String, HandlerType) -> LoggerException
        get() = ::LoggerException
}

internal inline fun <reified T: Any> T?.getOrThrowLogger(message: String):T{
   return this.getOrException(LoggerException(message, HandlerType.UNMANAGED))
}

internal inline fun <reified T: Any> T?.getOrThrow(message: String, handler: HandlerType):T{
  return  this.getOrException<T, ManagedException>(ManagedException(message, handler))
}

internal inline fun <reified T: Any> T?.getOrThrow(managedException: ManagedException):T{
    return  this.getOrException(managedException)
}

