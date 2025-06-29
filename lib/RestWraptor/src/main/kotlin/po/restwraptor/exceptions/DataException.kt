package po.restwraptor.exceptions

import com.typesafe.config.ConfigOrigin
import kotlinx.io.Source
import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManageableException
import po.misc.exceptions.ManagedException

class DataException(
    override var message: String,
    source: ExceptionCodes = ExceptionCodes.UNKNOWN,
    original: Throwable? = null
) : ManagedException(message, source, original) {

    override var handler: HandlerType = HandlerType.CancelAll


    companion object : ManageableException.Builder<DataException> {
        override fun build(message: String, source: Enum<*>?, original: Throwable?): DataException {
            val exCode = source as? ExceptionCodes ?: ExceptionCodes.UNKNOWN
            return DataException(message, exCode, original)
        }
    }

}