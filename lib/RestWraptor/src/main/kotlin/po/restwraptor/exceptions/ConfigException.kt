package po.restwraptor.exceptions

import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManageableException
import po.misc.exceptions.ManagedException


class ConfigurationException(
    override var message: String,
    code: ExceptionCodes = ExceptionCodes.UNKNOWN,
    original: Throwable? = null
) :  ManagedException(message, code, original){

    companion object : ManageableException.Builder<ConfigurationException> {
        override fun build(message: String, source: Enum<*>?,  original: Throwable?): ConfigurationException {
            val exCode = source as? ExceptionCodes ?: ExceptionCodes.UNKNOWN
            return ConfigurationException(message, exCode, original)
        }

    }

}