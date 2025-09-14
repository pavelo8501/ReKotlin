package po.misc.callbacks.events

import po.misc.types.TypeData
import kotlin.reflect.KClass


interface EventHost



class CallbackEvent<T: EventHost, R>(
   internal val host:T
) {

    internal var onEventSuspendedCallback: (suspend (T)-> R)? = null
    internal var onEventCallback: ((T)-> R)? = null

    var result:R? = null

    fun onEventSuspending(callback: suspend (T)-> R){
        onEventSuspendedCallback = callback
    }

    fun onEvent(callback: (T)-> R){
        onEventCallback = callback
    }

    suspend fun triggerSuspending(){
        result = onEventSuspendedCallback?.invoke(host)
    }

    fun trigger(){
        result = onEventCallback?.invoke(host)
    }
}


fun <T: EventHost> T.createEvent(
    callback: ((T)-> Unit)? = null
):CallbackEvent<T, Unit>{

    val event = CallbackEvent<T, Unit>(this)
    callback?.let {
        event.onEvent(it)
    }
  return  event
}

fun <T: EventHost> T.suspendableEvent(
    callback: (suspend (T)-> Unit)? = null
):CallbackEvent<T, Unit>{
    val event = CallbackEvent<T, Unit>(this)
    callback?.let {
        event.onEventSuspending(it)
    }
    return  event
}


class CallbackNotifier<H, T, R>(
    val  host:H,
    val  typeData: TypeData<T>
) where H: EventHost, T: Any  {

    var result:R? = null

    internal var onEventCallbackSuspending: (suspend H.(T)-> R)? = null
    internal var onEventCallback: (H.(T)-> R)? = null

    fun onEventSuspending(callback:suspend H.(T)-> R){
        onEventCallbackSuspending = callback
    }
    fun onEvent(callback: H.(T)-> R){
        onEventCallback = callback
    }

    suspend fun triggerSuspending(value: T){
        result = onEventCallbackSuspending?.invoke(host, value)
    }

    fun trigger(value: T){
        result = onEventCallback?.invoke(host, value)
    }
}

inline fun <H: EventHost, reified T: Any> H.suspendableNotification(
    kClass: KClass<T>,
    noinline callback: (suspend H.(T)-> Unit)? = null
):CallbackNotifier<H, T, Unit>{
    val notifier = CallbackNotifier<H, T, Unit>(this, TypeData.create())
    callback?.let {
        notifier.onEventSuspending(it)
    }
    return  notifier
}

inline fun <H: EventHost, reified T: Any> H.createNotification(
    kClass: KClass<T>,
    noinline callback: (H.(T)-> Unit)? = null
):CallbackNotifier<H, T, Unit>{
    val notifier =  CallbackNotifier<H, T, Unit>(this, TypeData.create())
    callback?.let {
        notifier.onEvent(it)
    }
    return  notifier
}

