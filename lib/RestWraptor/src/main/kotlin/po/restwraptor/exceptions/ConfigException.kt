package po.restwraptor.exceptions

import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException


class ConfigurationException(
    override var message: String,
    val code: ExceptionCodes = ExceptionCodes.UNKNOWN,
    var handlerType : HandlerType = HandlerType.CANCEL_ALL,
) :  ManagedException(message, handlerType){

    override val builderFn: (String, HandlerType) -> ConfigurationException = {msg, handler->
        ConfigurationException(msg, code, handler)
    }

}