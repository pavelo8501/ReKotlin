package po.lognotify.exceptions


import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException
import po.misc.exceptions.getOrException
import po.misc.safeCast
import kotlin.reflect.full.companionObjectInstance


class LoggerException(
    message: String,
) : ManagedException(message) {

    override var handler: HandlerType = HandlerType.UNMANAGED

    override val builderFn: (String, Int?) -> LoggerException ={message,_ ->
        LoggerException(message)
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

internal inline fun <reified T: Any> T?.getOrThrowLogger(message: String):T{
    return this.getOrException {
       throw LoggerException(message)
    }
}

internal inline fun <reified T: Any> T?.getOrThrow(message: String):T{
    return this.getOrException {
        throw ManagedException(message)
    }
}

internal inline fun <reified T: Any> T?.getOrThrow(managedException: ManagedException):T{
    return this.getOrException {
        throw managedException
    }
}

