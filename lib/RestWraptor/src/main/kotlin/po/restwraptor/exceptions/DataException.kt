package po.restwraptor.exceptions

import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException
import po.misc.safeCast
import kotlin.reflect.full.companionObjectInstance

class DataException(
    override var message: String,
    val code: ExceptionCodes = ExceptionCodes.UNKNOWN,

) : ManagedException(message) {

    override var handler: HandlerType = HandlerType.CANCEL_ALL

    override val builderFn: (String, Int?) -> DataException = {msg, code->

        val exCode = ExceptionCodes.fromValue(code?:0)

        DataException(msg, exCode)
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