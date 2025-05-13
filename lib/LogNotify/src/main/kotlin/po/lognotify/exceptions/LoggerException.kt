package po.lognotify.exceptions


import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException
import po.misc.exceptions.SelfThrownException
import po.misc.types.safeCast
import kotlin.reflect.full.companionObjectInstance


class LoggerException(
    message: String,
) : ManagedException(message) {

    override var handler: HandlerType = HandlerType.UNMANAGED

//    override val builderFn: (String, Int?) -> LoggerException ={message,_ ->
//        LoggerException(message)
//    }


    companion object : SelfThrownException.Builder<LoggerException> {
        override fun build(message: String, optionalCode: Int?): LoggerException {
            return LoggerException(message)
        }
    }

//    companion object {
//        inline fun <reified E : ManagedException> build(message: String, optionalCode: Int?): E {
//            return E::class.companionObjectInstance?.safeCast<Builder<E>>()
//                ?.build(message, optionalCode)
//                ?: throw IllegalStateException("Companion object must implement Builder<E> @ LoggerException")
//        }
//        interface Builder<E> {
//            fun build(message: String, optionalCode: Int?): E
//        }
//    }
}

