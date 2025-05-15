package po.restwraptor.exceptions

import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException
import po.misc.exceptions.SelfThrownException
import po.misc.types.safeCast
import kotlin.reflect.full.companionObjectInstance

class DataException(
    override var message: String,
    val code: ExceptionCodes = ExceptionCodes.UNKNOWN,

) : ManagedException(message) {

    override var handler: HandlerType = HandlerType.CANCEL_ALL


    companion object : SelfThrownException.Builder<DataException> {
        override fun build(message: String, optionalCode: Int?): DataException {
            val exCode = ExceptionCodes.getByValue(optionalCode ?: 0)
            return DataException(message, exCode)
        }
    }

}