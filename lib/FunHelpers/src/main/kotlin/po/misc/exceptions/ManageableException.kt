package po.misc.exceptions

import po.misc.types.safeCast
import kotlin.reflect.full.companionObjectInstance


sealed interface ManageableException<E:ManagedException> {

    interface Builder<E> {
        fun build(message: String, source:  Enum<*>?, original : Throwable?): E
    }

    companion object {

        @Deprecated("Unreliable", level =  DeprecationLevel.WARNING)
        inline fun <reified E : ManagedException, S: Enum<S>> build(message: String, source: S?, original : Throwable? = null): E {
            val newManaged = E::class.companionObjectInstance?.safeCast<Builder<E>>()?.build(message, source, original)
          return  newManaged?:run {
              val exceptionMessage =
                  "$message. Default ManagedException. Reason: ${E::class.simpleName.toString()} companion "+
                        "does not implement implement Builder<E> @ SelfThrownException"
              return ManagedException(exceptionMessage, null, original) as E
            }
        }
    }
}