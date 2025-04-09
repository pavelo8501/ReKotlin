package po.exposify.exceptions

import po.exposify.exceptions.enums.ExceptionCode
import po.lognotify.exceptions.CancellationException
import po.lognotify.exceptions.enums.HandlerType

class InitException(var msg: String, var errCode : ExceptionCode, var handlerType: HandlerType = HandlerType.CANCEL_ALL) :
    CancellationException(msg, handlerType, errCode.value)

class OperationsException(var msg: String, var errCode : ExceptionCode, var handlerType : HandlerType = HandlerType.CANCEL_ALL) :
   CancellationException (msg,  handlerType, errCode.value)