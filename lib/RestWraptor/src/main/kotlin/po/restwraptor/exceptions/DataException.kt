package po.restwraptor.exceptions

import po.lognotify.exceptions.ManagedException
import po.lognotify.exceptions.enums.HandlerType



class DataException(
    override var message: String,
    val code: ExceptionCodes = ExceptionCodes.UNKNOWN,
    override var handler: HandlerType,
) : ManagedException(message, handler) {

    override val builderFn: (String) -> DataException = {msg->
        DataException(msg, code, handler)
    }

}