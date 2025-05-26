package po.restwraptor.exceptions

import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException
import po.misc.exceptions.SelfThrownException
import po.misc.types.safeCast
import kotlin.reflect.full.companionObjectInstance


class ConfigurationException(
    override var message: String,
    val code: ExceptionCodes = ExceptionCodes.UNKNOWN,
) :  ManagedException(message){

    var handlerType : HandlerType = HandlerType.SKIP_SELF

    companion object : SelfThrownException.Builder<ConfigurationException> {
        override fun build(message: String, optionalCode: Int?): ConfigurationException {
            val exCode = ExceptionCodes.getByValue(optionalCode ?: 0)
            return ConfigurationException(message, exCode)
        }
    }
}