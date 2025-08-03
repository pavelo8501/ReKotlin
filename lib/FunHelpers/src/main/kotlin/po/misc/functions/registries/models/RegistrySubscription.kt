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

class TaggedSubscription<V: Any>(
    override val subscriberID: Long,
    val subscriberClass: KClass<*>,
    val function: (V)->Unit
): LambdaSubscriber<V>{
    val notifier: LambdaUnit<V, Unit> = Notifier(function)

    override fun trigger(value: V) {
        notifier.trigger(value)
    }

}

