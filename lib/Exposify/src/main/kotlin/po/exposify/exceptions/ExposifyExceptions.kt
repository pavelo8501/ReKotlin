package po.exposify.exceptions

import po.exposify.exceptions.enums.ExceptionCode
import po.managedtask.exceptions.CancellationException
import po.managedtask.exceptions.ExceptionBase

import po.managedtask.exceptions.enums.CancelType

class InitException(var msg: String, var errCode : ExceptionCode, var handlerType: CancelType = CancelType.CANCEL_ALL) :
    ExceptionBase.Cancellation(msg, handlerType, errCode.value)

class OperationsException(var msg: String, var errCode : ExceptionCode, var handlerType : CancelType = CancelType.CANCEL_ALL) :
    ExceptionBase.Cancellation (msg,  handlerType, errCode.value)