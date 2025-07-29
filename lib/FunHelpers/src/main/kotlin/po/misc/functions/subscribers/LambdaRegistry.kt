package po.misc.functions.subscribers

import po.misc.context.CTX
import po.misc.functions.containers.LambdaContainer
import po.misc.functions.containers.Notifier
import po.misc.functions.containers.TaggedNotifier
import po.misc.types.Typed
import java.util.EnumMap
import kotlin.reflect.KClass


class LambdaSubscription<V: Any, R: Any>(
    val subscriberID: Long,
    val kClass: KClass<*>,
    val lambda: LambdaContainer<V, R>
)



sealed class LambdaRegistryBase<K: Any,  V: Any>(){

    abstract val notifiers: Map<K, LambdaSubscription<V, Unit>>

    fun trigger(event:K,  value:V){
        notifiers[event]?.lambda?.trigger(value)
    }

    fun trigger(subscriberId: Long,  event:K,  value:V){
        notifiers[event]?.takeIf { it.subscriberID == subscriberId }?.lambda?.trigger(value)
    }

    abstract fun subscribe(subscriberId: Long, kClass: KClass<*>,  key: K,  callback:(V)-> Unit):LambdaSubscription<V, Unit>
}


enum class DefaultEvent {
    Default
}

class LambdaRegistry<V: Any>(
    val defaultKey:DefaultEvent = DefaultEvent.Default,
):LambdaRegistryBase<DefaultEvent, V>() {


    override val notifiers: MutableMap<DefaultEvent, LambdaSubscription<V, Unit>> = mutableMapOf()

    override fun subscribe(subscriberId: Long, kClass: KClass<*>, key: DefaultEvent, callback:(V)-> Unit):LambdaSubscription<V, Unit>{
        val subscription =  LambdaSubscription(subscriberId, kClass,  Notifier(callback))

        notifiers.put(key, subscription)
        return subscription
    }

    fun subscribe(subscriberId: Long, context: CTX, callback:(V)-> Unit):LambdaSubscription<V, Unit>{
       return subscribe(subscriberId, context.identity.kClass, defaultKey, callback)
    }
}


fun <V: Any>  LambdaRegistry<V>.subscribe(
    context: CTX,
    callback:(V)-> Unit
){
    subscribe(context.identity.numericId, context, callback)
}


class TaggedLambdaRegistry<E: Enum<E>, V: Any>(
    keyType: Class<E>
):LambdaRegistryBase<E, V>() {

    override val notifiers: EnumMap<E, LambdaSubscription<V, Unit>> = EnumMap<E, LambdaSubscription<V, Unit>>(keyType)

    override fun subscribe(subscriberId: Long, kClass: KClass<*>, key:E, callback:(V)-> Unit):LambdaSubscription<V, Unit>{
        val subscription =  LambdaSubscription(subscriberId, kClass, TaggedNotifier(key, callback))
        notifiers.put(key,subscription)
        return subscription
    }
}

fun <E: Enum<E>, V: Any>  TaggedLambdaRegistry<E, V>.subscribe(
    context: CTX,
    event:E,
    callback:(V)-> Unit
){
    subscribe(context.identity.numericId, context.identity.kClass, event, callback)
}