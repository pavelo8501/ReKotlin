package po.misc.exceptions

import po.misc.safeCast
import kotlin.reflect.full.companionObjectInstance


enum class HandlerType(val value: Int) {

    GENERIC(0),
    SKIP_SELF(1),
    CANCEL_ALL(2),
    UNMANAGED(3);
    companion object {
        fun fromValue(value: Int): HandlerType {
            entries.firstOrNull { it.value == value }?.let {
                return it
            }
            return GENERIC
        }
    }
}

sealed interface SelfThrownException<E:ManagedException>  {
    val message: String
    var handler  : HandlerType
    val builderFn: (String, HandlerType) -> E

    fun setSourceException(th: Throwable): E
    fun throwSelf(): Nothing


    companion object {
        inline fun <reified E : ManagedException> build(message: String, handler: HandlerType): E {
            return E::class.companionObjectInstance?.safeCast<Builder<E>>()
                ?.build(message, handler)
                ?: throw IllegalStateException("Companion object must implement Builder<E>")
        }

        interface Builder<E> {
            fun build(message: String, handler: HandlerType): E
        }
    }
}

open class ManagedException(
    override var message: String,
    override var handler  : HandlerType,
) : Throwable(message), SelfThrownException<ManagedException>{

    override val builderFn: (String, HandlerType) -> ManagedException={msg, handler->
        ManagedException(msg, handler)
    }

    fun addMessage(newMessage: String): ManagedException{
        message = "$message. $newMessage"
        return this
    }

    private var source : Throwable? = null
    override fun setSourceException(th: Throwable): ManagedException{
        source = th
        return this
    }
    override fun throwSelf():Nothing {
        throw this
    }

}