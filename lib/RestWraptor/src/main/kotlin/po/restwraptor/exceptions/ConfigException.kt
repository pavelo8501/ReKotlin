package po.restwraptor.exceptions

import po.lognotify.exceptions.ManagedException
import po.lognotify.exceptions.enums.HandlerType


class ConfigurationException(
    override var message: String,
    val code: ExceptionCodes = ExceptionCodes.UNKNOWN,
    var handlerType : HandlerType = HandlerType.CANCEL_ALL,
) :  ManagedException(message, handlerType){

    override val builderFn: (String, HandlerType) -> ConfigurationException = {msg, handler->
        ConfigurationException(msg, code, handler)
    }

}