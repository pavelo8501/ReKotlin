package po.misc.functions.subscribers

import po.misc.functions.registries.LambdaSubscriber


class NotifySubscription<S: LambdaSubscriber<V>, V: Any>(
    val subscriber: S
){
    fun trigger(value:V){
        subscriber.trigger(value)
    }

}