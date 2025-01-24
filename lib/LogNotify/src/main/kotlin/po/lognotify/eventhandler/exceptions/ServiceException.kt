package po.lognotify.eventhandler.exceptions

import po.lognotify.shared.enums.HandleType
import po.lognotify.shared.exceptions.SelfThrowableException


data class SkipException(
    val msg: String,
) : ProcessableException(msg, HandleType.SKIP_SELF)

data class CancelException(
    val msg: String,
): ProcessableException(msg, HandleType.CANCEL_ALL)

data class PropagateException(
    val msg: String,
): ProcessableException(msg, HandleType.PROPAGATE_TO_PARENT)

data class NotificatorUnhandledException(
    override val message: String,
    override val cause: Throwable? = null
) : Exception(message, cause), SelfThrowableException

abstract class ProcessableException(
    override var message: String,
    var handleType: HandleType
) : Exception(message), SelfThrowableException




