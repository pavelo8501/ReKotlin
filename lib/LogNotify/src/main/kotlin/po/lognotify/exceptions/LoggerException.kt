package po.lognotify.exceptions


import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException
import po.misc.exceptions.getOrException


class LoggerException(
    message: String,
) : ManagedException(message) {

    override var handler: HandlerType = HandlerType.UNMANAGED

    override val builderFn: (String, Int?) -> LoggerException ={message,_ ->
        LoggerException(message)
    }



}

internal inline fun <reified T: Any> T?.getOrThrowLogger(message: String):T{
    return this.getOrException {
       throw LoggerException(message)
    }
}

internal inline fun <reified T: Any> T?.getOrThrow(message: String):T{
    return this.getOrException {
        throw ManagedException(message)
    }
}

internal inline fun <reified T: Any> T?.getOrThrow(managedException: ManagedException):T{
    return this.getOrException {
        throw managedException
    }
}

