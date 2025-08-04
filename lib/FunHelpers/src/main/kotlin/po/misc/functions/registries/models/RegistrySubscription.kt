package po.misc.functions.registries.models

import po.misc.functions.containers.LambdaUnit
import po.misc.functions.containers.Notifier
import po.misc.functions.registries.LambdaSubscriber
import kotlin.reflect.KClass


class RegistrySubscription<V: Any>(
    override val subscriberID: Long,
    val notifier: LambdaUnit<V, Unit>
): LambdaSubscriber<V>{

    override fun trigger(value: V) {
        notifier.trigger(value)
    }
}

interface TaggedSubscription<V: Any>:LambdaSubscriber<V>{
    val subscriberClass: KClass<*>
}

class Subscription<V: Any>(
    override val subscriberID: Long,
    override val subscriberClass: KClass<*>,
    val function: (V)->Unit
): TaggedSubscription<V>{
    val notifier: LambdaUnit<V, Unit> = Notifier(function)

    override fun trigger(value: V) {
        notifier.trigger(value)
    }
}

class SuspendSubscription<V: Any>(
    override val subscriberID: Long,
    override val subscriberClass: KClass<*>,
    val notifier: LambdaUnit<V, Unit>
): TaggedSubscription<V>{

    override fun trigger(value: V) {
        notifier.trigger(value)
    }
}


