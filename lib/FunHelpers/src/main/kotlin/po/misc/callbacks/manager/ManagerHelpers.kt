package po.misc.callbacks.manager

import po.misc.interfaces.IdentifiableContext
import po.misc.types.castOrManaged


inline fun<reified E: Enum<E>> IdentifiableContext.callbackManager(
    vararg payloads :  CallbackPayloadBase<E, *, *>  = emptyArray()
): CallbackManager<E>{
    return CallbackManager<E>(E::class.java, this, payloads.castOrManaged())
}

fun<E: Enum<E>> IdentifiableContext.callbackManager(
    eventType:E,
    vararg payloadBuilder : E.() -> CallbackPayloadBase<E, *,* >
): CallbackManager<E>{
    val event = eventType::class.java.castOrManaged<Class<E>>()

    val manager = CallbackManager<E>(event, this)
    payloadBuilder.forEach {builder->
        val payload =  builder.invoke(eventType)
        manager.addPayload(payload)
    }
    return manager
}

inline fun<reified E: Enum<E>> IdentifiableContext.withCallbackManager(
    payloadBuilder : CallbackManager<E>.() -> Unit
):CallbackManager<E>{
    val manager = CallbackManager<E>(E::class.java, this)
    payloadBuilder.invoke(manager)
    return manager
}

fun<E: Enum<E>, T: Any> E.payload(
    block: (CallbackPayload<E,T>.()-> CallbackPayload<E,T>)? = null
): CallbackPayload<E,T>{
    val payload  = CallbackPayload<E, T>(this)
    return block?.invoke(payload) ?: payload
}

fun<E: Enum<E>, T: Any> CallbackManager<E>.withPayload(
    eventType: E,
    block: (CallbackPayload<E,T>.()-> Unit)
): CallbackPayload<E,T>{
    val createdPayload =  this.payload<T>(eventType)
    block.invoke(createdPayload)
    return createdPayload
}

fun<E: Enum<E>, T: Any, R: Any> IdentifiableContext.payloadWithResult(eventType:E): ResultCallbackPayload<E, T, R>{
    val payload  = ResultCallbackPayload<E, T, R>(eventType)
    return payload
}

fun <T: CallbackManager<*>> T.managerHooks(hooks: CallbackManagerHooks.()-> Unit){
    val hooksInstance = CallbackManagerHooks().apply(hooks)
    this.hooks = hooksInstance
}


