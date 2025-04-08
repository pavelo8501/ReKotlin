package po.exposify.exceptions

import po.exposify.exceptions.enums.ExceptionCode
import po.lognotify.exceptions.ExceptionBase
import po.lognotify.exceptions.enums.CancelType

class InitException(var msg: String, var errCode : ExceptionCode, var handlerType: CancelType = CancelType.CANCEL_ALL) :
    ExceptionBase.Cancellation(msg, handlerType, errCode.value)

class OperationsException(var msg: String, var errCode : ExceptionCode, var handlerType : CancelType = CancelType.CANCEL_ALL) :
    ExceptionBase.Cancellation (msg,  handlerType, errCode.value)