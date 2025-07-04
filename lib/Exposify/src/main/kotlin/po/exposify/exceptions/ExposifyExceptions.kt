package po.exposify.exceptions

import po.exposify.exceptions.enums.ExceptionCode
import po.misc.exceptions.HandlerType

import po.misc.exceptions.ManagedException
import po.misc.exceptions.ManageableException

class InitException(var msg: String, source : ExceptionCode, original: Throwable? ) :
    ManagedException(msg, source, original)
{
    override var handler: HandlerType = HandlerType.SkipSelf
    companion object : ManageableException.Builder<InitException> {
        override fun build(message: String, source: Enum<*>?,  original: Throwable?): InitException {
            val exCode = source as? ExceptionCode ?: ExceptionCode.UNDEFINED
            return InitException(message, exCode, original)
        }
    }
}

class OperationsException(var msg: String,  source : ExceptionCode, original: Throwable?) :
    ManagedException (msg, source, original)
{
    override var handler : HandlerType = HandlerType.SkipSelf

    companion object : ManageableException.Builder<OperationsException> {
        override fun build(message: String, source: Enum<*>?,  original: Throwable?): OperationsException {
            val exCode = source as? ExceptionCode ?: ExceptionCode.UNDEFINED
            return OperationsException(message, exCode, original)
        }
    }

}