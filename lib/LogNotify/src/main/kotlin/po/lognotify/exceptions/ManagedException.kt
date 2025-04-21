package po.lognotify.exceptions

import po.lognotify.exceptions.enums.HandlerType
import java.util.Objects


sealed interface SelfThrownException {
    val message: String
    var handler  : HandlerType
    val builderFn: (String, HandlerType) -> SelfThrownException

    fun setSourceException(th: Throwable): ManagedException
    fun throwSelf(): Nothing

}

open class ManagedException(
    override val message: String,
    override var handler  : HandlerType,
) : Throwable(message), SelfThrownException{

    override val builderFn: (String, HandlerType) -> ManagedException={msg, handler->
        ManagedException(msg, handler)
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