package po.restwraptor.exceptions

import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManageableException
import po.misc.exceptions.ManagedException


class ConfigurationException(
    override var message: String,
    code: ExceptionCodes = ExceptionCodes.UNKNOWN,
    original: Throwable?
) :  ManagedException(message, code, original){

    var handlerType : HandlerType = HandlerType.SKIP_SELF

    companion object : ManageableException.Builder<ConfigurationException> {
        override fun build(message: String, source: Enum<*>?,  original: Throwable?): ConfigurationException {
            val exCode = source as? ExceptionCodes ?: ExceptionCodes.UNKNOWN
            return ConfigurationException(message, exCode, original)
        }

    }

}