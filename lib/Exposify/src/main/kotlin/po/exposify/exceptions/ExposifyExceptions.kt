package po.exposify.exceptions

import po.exposify.exceptions.enums.ExceptionCode
import po.lognotify.exceptions.ManagedException
import po.lognotify.exceptions.enums.HandlerType

class InitException(var msg: String, var errCode : ExceptionCode, override var handler: HandlerType = HandlerType.CANCEL_ALL) :
    ManagedException(msg, handler){

        override val builderFn: (String, HandlerType) -> InitException={msg, handler->
            InitException(msg, errCode, handler)
        }

    }

class OperationsException(var msg: String, var errCode : ExceptionCode, override var handler : HandlerType = HandlerType.CANCEL_ALL) :
    ManagedException (msg,  handler){

        override val builderFn: (String, HandlerType) -> OperationsException={msg, handler->
            OperationsException(msg,errCode, handler)
        }
    }