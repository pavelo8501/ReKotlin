package po.misc.exceptions

import po.misc.types.safeCast
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
    val builderFn: (String, Int?) -> E

    fun setSourceException(th: Throwable): E
    fun getSourceException(returnSelfIfNull: Boolean): Throwable?
    fun setHandler(handlerType: HandlerType): E
    fun throwSelf(): Nothing


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

open class ManagedException(
    override var message: String,
) : Throwable(message), SelfThrownException<ManagedException>{

    override var handler  : HandlerType = HandlerType.CANCEL_ALL

    override val builderFn: (String, Int?) -> ManagedException={msg,_->
        ManagedException(msg)
    }

    fun addMessage(newMessage: String): ManagedException{
        message = "$message. $newMessage"
        return this
    }

    override fun setHandler(handlerType: HandlerType): ManagedException{
        handler = handlerType
        return this
    }

    private var source : Throwable? = null
    override fun setSourceException(th: Throwable): ManagedException{
        source = th
        return this
    }
    override fun getSourceException(returnSelfIfNull: Boolean): Throwable?{
        if(returnSelfIfNull){
            return source?:this
        }
        return source
    }

    override fun throwSelf():Nothing {
        throw this
    }
}