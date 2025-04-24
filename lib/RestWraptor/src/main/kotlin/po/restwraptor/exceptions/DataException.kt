package po.restwraptor.exceptions

import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException

class DataException(
    override var message: String,
    val code: ExceptionCodes = ExceptionCodes.UNKNOWN,
    override var handler: HandlerType,
) : ManagedException(message, handler) {

    override val builderFn: (String, HandlerType) -> DataException = {msg, handler->
        DataException(msg, code, handler)
    }

}