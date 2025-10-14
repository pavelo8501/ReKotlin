package po.misc.callbacks.containers

import po.misc.callbacks.events.CallbackEventBase
import po.misc.callbacks.events.EventHost
import po.misc.callbacks.events.ParametrizedEvent
import po.misc.exceptions.handling.Suspended
import kotlin.reflect.KClass


interface ListenerContainer<H: Any, T: Any, R: Any>{

   // val eventLambda: H.(T)->R
  //  val suspendedEventLambda: suspend H.(T)->R

    fun triggerEvent(value:T)
    suspend fun triggerEvent(value:T, suspended: Suspended)
}


class EventListeners<H: EventHost, T: Any, R: Any>(val event: ParametrizedEvent<H, T, R>): ListenerContainer<H, T, R>{

    private val listenersSuspending = mutableMapOf<KClass<*>, suspend H.(T)->R>()
    private val listeners = mutableMapOf<KClass<*>, H.(T)->R>()

    fun onEvent(listener: KClass<*>, lambda: suspend H.(T)->R): suspend H.(T)->R{
       return listenersSuspending.getOrPut(listener, { lambda })
    }

    override fun triggerEvent(value:T){
        listeners.values.forEach {
            it.invoke(event.host, value)
        }
    }

    override suspend fun triggerEvent(value:T, suspended: Suspended){
        listenersSuspending.values.forEach {
            triggerEvent(value)
            it.invoke(event.host, value)
        }
    }

}
