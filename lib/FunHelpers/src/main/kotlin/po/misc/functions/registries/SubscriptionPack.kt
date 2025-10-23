package po.misc.functions.registries

import po.misc.functions.registries.models.TaggedSubscriber
import po.misc.types.token.TypeToken
import kotlin.reflect.KClass


sealed interface SubscriptionPack<V: Any>{
    val subscriber: KClass<*>
    val subscriptions: List<Pair<RegistryKey,  (V)-> Unit>>
}

class EmitterSubscriptions<V: Any>(
    override val subscriber: KClass<*>,
    val targetEmitter: TypeToken<*>
):SubscriptionPack<V>{

    val subscriptionsBacking: MutableList<Pair<TaggedSubscriber<*>, (V)-> Unit>> = mutableListOf()

    override val subscriptions: List<Pair<RegistryKey,  (V)-> Unit >> = subscriptionsBacking as List<Pair<RegistryKey, (V)-> Unit>>

    fun <E: Enum<E>> addSubscription(
        subscriber: TaggedSubscriber<E>,
        callback:(V)-> Unit
    ){
        subscriptionsBacking.add(Pair(subscriber, callback))
    }
}