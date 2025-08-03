package po.misc.functions.registries

import po.misc.context.CTX
import po.misc.functions.containers.Notifier
import po.misc.functions.registries.models.RegistrySubscription
import po.misc.functions.registries.models.TaggedSubscription
import java.util.EnumMap
import kotlin.reflect.KClass


sealed class LambdaRegistryBase<K: Any,  V: Any>(){

    protected abstract val notifiers: Map<K, List<LambdaSubscriber<V>>>
    val size: Int get() = notifiers.size

    fun trigger(key:K,  value:V){
        notifiers[key]?.forEach { it.trigger(value)}
    }
    fun triggerAll(value:V){
        notifiers.values.flatMap { it }.forEach { it.trigger(value) }
    }


    abstract fun clear()
}

class NotifierRegistry<V: Any>():LambdaRegistryBase<KClass<*>, V>() {

    override val notifiers: MutableMap<KClass<*>, MutableList<LambdaSubscriber<V>>> = mutableMapOf()

    fun subscribe(subscriberId: Long, subscriber: Any, callback:(V)-> Unit):LambdaSubscriber<V>{
        val subscription = RegistrySubscription(subscriberId, Notifier(callback))
        notifiers.getOrPut(subscriber::class) { mutableListOf() }.add(subscription)
        return subscription
    }


    fun subscribe(subscriberId: Long, context: CTX, callback:(V)-> Unit):LambdaSubscriber<V> =
        subscribe(subscriberId, context.identity.kClass, callback)


    override fun clear() {
        notifiers.clear()
    }
}

fun <V: Any>  NotifierRegistry<V>.subscribe(
    context: CTX,
    callback:(V)-> Unit
){
    subscribe(context.identity.numericId, context, callback)
}


class TaggedNotifierRegistry<E: Enum<E>,  V: Any>(
    keyType: Class<E>,
):LambdaRegistryBase<E, V>() {

    override val notifiers: EnumMap<E, MutableList<TaggedSubscription<V>>> = EnumMap(keyType)

    private fun nextId(event:E): Long{
       return (notifiers[event]?.maxOfOrNull { it.subscriberID } ?: 0L) +1
    }


    fun trigger(event:E, subscriberId: Long, subscriberClass: KClass<*>, value:V){
        notifiers[event]
            ?.firstOrNull { it.subscriberID == subscriberId && it.subscriberClass == subscriberClass }
            ?.trigger(value)
    }

    fun subscribe(event:E, subscription:TaggedSubscription<V>){
        notifiers.getOrPut(event) { mutableListOf() }.add(subscription)
    }

    fun subscribe(event:E,  subscriberId: Long, subscriber: Any, callback:(V)-> Unit): TaggedSubscription<V>{
        val subscription: TaggedSubscription<V> = TaggedSubscription(subscriberId, subscriber::class, callback)
        notifiers.getOrPut(event) { mutableListOf() }.add(subscription)
        return subscription
    }
    fun subscribe(event:E,  subscriber: Any, callback:(V)-> Unit):TaggedSubscription<V> =
        subscribe(event,  nextId(event), subscriber, callback)


    override fun clear() {
        notifiers.clear()
    }
}


inline fun <reified E: Enum<E>, V: Any> taggedRegistryOf():TaggedNotifierRegistry<E, V>{
   return TaggedNotifierRegistry<E,V>(E::class.java)
}

fun <E: Enum<E>, V: Any>  TaggedNotifierRegistry<E, V>.subscribe(
    event:E,
    context: Any,
    subscriberId: Long,
    callback:(V)-> Unit
){
    subscribe(event, subscriberId, context::class, callback)
}

fun <E: Enum<E>, V: Any>  TaggedNotifierRegistry<E, V>.subscribe(
    event:E,
    context: CTX,
    callback:(V)-> Unit
){
    subscribe(event, context.identity.numericId, context.identity.kClass, callback)
}