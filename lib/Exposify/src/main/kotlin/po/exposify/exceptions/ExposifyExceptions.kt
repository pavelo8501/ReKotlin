package po.exposify.exceptions

import po.exposify.exceptions.enums.ExceptionCode
import po.misc.exceptions.HandlerType

import po.misc.exceptions.ManagedException
import po.misc.exceptions.SelfThrownException
import po.misc.types.safeCast
import kotlin.reflect.full.companionObjectInstance

class InitException(var msg: String, var exceptionCode : ExceptionCode) :
    ManagedException(msg)
{

    override var handler: HandlerType = HandlerType.SKIP_SELF

    companion object : SelfThrownException.Builder<InitException> {
        override fun build(message: String, optionalCode: Int?): InitException {
            val exCode = ExceptionCode.getByValue(optionalCode ?: 0)
            return InitException(message, exCode)
        }
    }
}

class OperationsException(var msg: String, var exceptionCode : ExceptionCode) :
    ManagedException (msg)
{
    override var handler : HandlerType = HandlerType.SKIP_SELF

    companion object : SelfThrownException.Builder<OperationsException> {
        override fun build(message: String, optionalCode: Int?): OperationsException {
            val exCode = ExceptionCode.getByValue(optionalCode ?: 0)
            return OperationsException(message, exCode)
        }
    }

}