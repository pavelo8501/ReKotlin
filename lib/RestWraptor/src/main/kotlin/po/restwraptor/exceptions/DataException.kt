package po.restwraptor.exceptions


import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException

class DataException(
    override var message: String,
    source: ExceptionCodes = ExceptionCodes.UNKNOWN,
    original: Throwable? = null
) : ManagedException(message, source, original) {

    override var handler: HandlerType = HandlerType.CancelAll

}