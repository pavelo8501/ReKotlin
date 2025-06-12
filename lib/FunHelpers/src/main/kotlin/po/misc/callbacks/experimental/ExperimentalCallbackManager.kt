package po.misc.callbacks.experimental

import java.util.EnumMap


enum class ExpEvent{
    OnInit,
    OnComplete
}


/**
 * Will include routing information for complex transfers and other arbitrary data
 * Additionally can be extended with lambda with receiver variations
 * Later can be interchanged to Flow based container
 */
data class CallableContainer<T, R>(
    var callback: ((T)-> R)? = null
){

}

/**
 * Extendable callback payload. At the moment two types with result and without
 * Later can be interchanged to Flow based container
 */
abstract class ExperimentalPayloadBase<T, R, E: Enum<E>>(
    val eventType: E,
    val container: CallableContainer<T, R>
){
    abstract val withResult: Boolean

    fun subscribe(function: (T)-> R){
        container.callback = function
    }

}

class ExperimentalPayload<T: Any, E: Enum<E>>(
    eventType : E,
    container: CallableContainer<T, Unit>
): ExperimentalPayloadBase<T, Unit, E>(eventType, container){

    override val withResult: Boolean = false
}


class ExperimentalResultPayload<T: Any, R: Any,E: Enum<E>>(
    eventType : E,
    container: CallableContainer<T, R>
): ExperimentalPayloadBase<T, R, E>(eventType, container){

    override val withResult: Boolean = true
}



class ExperimentalCallbackManager<P: ExperimentalPayloadBase<*, *, E>, E: Enum<E>>(
    enumClass: Class<E>,
    vararg val payloads : P
) {

    private val enumMap = EnumMap<E, ExperimentalPayloadBase<*, *, E>>(enumClass).apply {
        payloads.forEach { put(it.eventType, it) }
    }

    fun <T: Any> getPayload(eventType:E):ExperimentalPayload<T, E>{
        return payloads.toList().filterIsInstance<ExperimentalPayload<T, E>>().first { it.eventType == eventType }
    }

    @JvmName("getPayloadWithResult")
    fun <T: Any, R: Any> getPayload(eventType:E):ExperimentalResultPayload<T, R, E>{
       return payloads.toList().filterIsInstance<ExperimentalResultPayload<T, R, E>>().first { it.eventType == eventType }
    }
}