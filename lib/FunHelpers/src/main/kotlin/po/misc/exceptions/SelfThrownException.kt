package po.misc.exceptions

import po.misc.exceptions.ManagedException.ExceptionEvent
import po.misc.interfaces.Identifiable
import po.misc.types.safeCast
import kotlin.reflect.full.companionObjectInstance


sealed interface SelfThrownException<E:ManagedException>  {
    var propertySnapshot :  Map<String, Any?>
    fun setHandler(handlerType: HandlerType,  wayPoint: Identifiable): E
    fun throwSelf(wayPoint: Identifiable): Nothing

    fun addHandlingData(waypoint: Identifiable, event: ExceptionEvent, message: String? = null): ManagedException

    interface Builder<E> {
        fun build(message: String, source:  Enum<*>?, original : Throwable?): E
    }

    companion object {
        inline fun <reified E : ManagedException> build(message: String, source:  Enum<*>?, original : Throwable? = null): E {
            val newManaged = E::class.companionObjectInstance?.safeCast<Builder<E>>()?.build(message, source, original)
            return newManaged?:throw IllegalStateException("Companion object must implement Builder<E> @ SelfThrownException")
        }
    }
}