package po.lognotify.exceptions

import po.lognotify.exceptions.enums.HandlerType


class LoggerException(
    message: String,
    handler: HandlerType = HandlerType.UNMANAGED
) : ManagedException(message, handler) {

    override val builderFn: (String) -> LoggerException
        get() = ::LoggerException

}