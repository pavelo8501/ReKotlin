package po.lognotify.shared.exceptions

import po.lognotify.shared.enums.HandleType

abstract class ServiceException(msg: String) : HandledThrowable(msg, HandleType.CANCEL_ALL, null)

sealed class HandledThrowable(
    val msg: String,
    val type : HandleType = HandleType.SKIP_SELF,
    cause: Throwable?) : Throwable(msg, cause)