package po.lognotify.exceptions


import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException
import po.misc.exceptions.getOrException


class LoggerException(
    message: String,
    handler: HandlerType = HandlerType.UNMANAGED
) : ManagedException(message, handler) {

    override val builderFn: (String, HandlerType) -> LoggerException
        get() = ::LoggerException
}

internal inline fun <reified T: Any> T?.getOrThrowLogger(message: String):T{
    return this.getOrException {
       throw LoggerException(message, HandlerType.UNMANAGED)
    }
}

internal inline fun <reified T: Any> T?.getOrThrow(message: String, handler: HandlerType):T{
    return this.getOrException {
        throw ManagedException(message, handler)
    }
}

internal inline fun <reified T: Any> T?.getOrThrow(managedException: ManagedException):T{
    return this.getOrException {
        throw managedException
    }
}

