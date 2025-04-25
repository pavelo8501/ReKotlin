package po.exposify.exceptions

import po.exposify.exceptions.enums.ExceptionCode
import po.misc.exceptions.HandlerType

import po.misc.exceptions.ManagedException
import po.misc.safeCast
import kotlin.reflect.full.companionObjectInstance

class InitException(var msg: String, var exceptionCode : ExceptionCode) :
    ManagedException(msg) {

    override var handler: HandlerType = HandlerType.SKIP_SELF

    override val builderFn: (String, Int?) -> InitException = { msg, code ->
        val exCode = ExceptionCode.getByValue(code?:0)
        InitException(msg, exCode)
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

class OperationsException(var msg: String, var exceptionCode : ExceptionCode) :
    ManagedException (msg){

    override var handler : HandlerType = HandlerType.SKIP_SELF
        override val builderFn: (String, Int?) -> OperationsException={msg, code->
            val exCode =  ExceptionCode.getByValue(code?:0)
            OperationsException(msg, exCode)
        }
    }