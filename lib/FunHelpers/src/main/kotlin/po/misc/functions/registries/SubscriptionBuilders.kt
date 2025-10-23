package po.misc.functions.registries

import po.misc.context.CTX
import po.misc.functions.registries.models.TaggedSubscriber
import po.misc.types.token.TypeToken


fun <V: Any> CTX.buildSubscriptions(
    targetEmitter: TypeToken<*>,
    builder: SubscriptionPack<V>.()-> Unit
):SubscriptionPack<V>{
    val pack = EmitterSubscriptions<V>(identity.typeData.kClass, targetEmitter)
    pack.builder()
   return pack
}


fun <E: Enum<E>, V: Any> SubscriptionPack<V>.addHook(
    tag:E,
    oneShot: Boolean,
    callback: (V)-> Unit
):TaggedSubscriber<E> {
   val subscription = TaggedSubscriber(tag, subscriber, oneShot)

    when(this){
        is EmitterSubscriptions -> {
            addSubscription(subscription, callback)
        }
    }
   return subscription
}

