package po.lognotify.exceptions


import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException
import po.misc.exceptions.SelfThrownException


class LoggerException(
    message: String,
) : ManagedException(message) {

    override var handler: HandlerType = HandlerType.UNMANAGED

    companion object : SelfThrownException.Builder<LoggerException> {
        override fun build(message: String, optionalCode: Int?): LoggerException {
            return LoggerException(message)
        }
    }

}

