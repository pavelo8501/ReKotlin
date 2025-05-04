package po.restwraptor.exceptions

import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException
import po.misc.types.safeCast
import kotlin.reflect.full.companionObjectInstance


class ConfigurationException(
    override var message: String,
    val code: ExceptionCodes = ExceptionCodes.UNKNOWN,
) :  ManagedException(message){


    var handlerType : HandlerType = HandlerType.SKIP_SELF

    override val builderFn: (String, Int?) -> ConfigurationException = {msg, code->
        val exCode = ExceptionCodes.fromValue(code?:0)
        ConfigurationException(msg, exCode)
    }

    companion object {
        inline fun <reified E : ManagedException> build(message: String, optionalCode: Int?): E {
            return E::class.companionObjectInstance?.safeCast<Builder<E>>()
                ?.build(message, optionalCode)
                ?: throw IllegalStateException("Companion object must implement Builder<E>")
        }

        interface Builder<E> {
            fun build(message: String, optionalCode: Int?): E
        }
    }

}