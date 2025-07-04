package po.misc.exceptions

import po.misc.exceptions.ManagedException.ExceptionEvent
import po.misc.interfaces.Identifiable
import po.misc.interfaces.IdentifiableContext
import po.misc.types.safeCast
import kotlin.reflect.full.companionObjectInstance


sealed interface ManageableException<E:ManagedException> {

   // val message: String
    var propertySnapshot :  Map<String, Any?>
    fun setHandler(handlerType: HandlerType,  wayPoint: IdentifiableContext): E
    fun throwSelf(wayPoint: IdentifiableContext): Nothing

    fun addHandlingData(waypoint: IdentifiableContext, event: ExceptionEvent, message: String? = null): ManagedException

    interface Builder<E> {
        fun build(message: String, source:  Enum<*>?, original : Throwable?): E
    }

    companion object {
        inline fun <reified E : ManagedException, S: Enum<S>> build(message: String, source: S?, original : Throwable? = null): E {
            val newManaged = E::class.companionObjectInstance?.safeCast<Builder<E>>()?.build(message, source, original)
          return  newManaged?:run {
              val exceptionMessage =
                  "$message. Default ManagedException. Reason: ${E::class.simpleName.toString()} companion "+
                        "does not implement implement Builder<E> @ SelfThrownException"
              return ManagedException(exceptionMessage, source, original) as E
            }

        }
    }
}