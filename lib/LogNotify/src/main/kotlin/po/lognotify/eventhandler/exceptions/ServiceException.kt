package po.lognotify.eventhandler.exceptions

import po.lognotify.shared.enums.HandleType
import po.lognotify.shared.exceptions.SelfThrowableException


internal data class SkipException(
    val msg: String,
) : ProcessableException(HandleType.SKIP_SELF, msg)

internal data class CancelException(
    val msg: String,
): ProcessableException(HandleType.CANCEL_ALL, msg){
    var cancelFn: () -> Unit = {}
}

internal data class PropagateException(
    val msg: String,
): ProcessableException(HandleType.PROPAGATE_TO_PARENT, msg)

internal data class UnmanagedException(
    override val message: String,
    override val cause: Throwable? = null
) : Exception(message, cause), SelfThrowableException

abstract class ProcessableException(
    var handleType: HandleType,
    override var message: String,
    val errorCode : Int = 0
) : Exception(message), SelfThrowableException{
    var cancellationFn: (() -> Unit) = {}
}




