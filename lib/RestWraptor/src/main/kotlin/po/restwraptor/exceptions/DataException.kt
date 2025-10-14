package po.restwraptor.exceptions


import po.misc.context.CTX
import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException

class DataException(
    context: CTX,
    override var message: String,
    code: ExceptionCodes = ExceptionCodes.UNKNOWN,
    cause: Throwable? = null
) : ManagedException(context,  message, code, cause) {

    override var handler: HandlerType = HandlerType.CancelAll

}