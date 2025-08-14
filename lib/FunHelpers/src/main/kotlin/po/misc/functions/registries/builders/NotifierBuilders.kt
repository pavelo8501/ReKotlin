package po.misc.functions.registries.builders

import po.misc.context.CTX
import po.misc.exceptions.ManagedException
import po.misc.functions.models.NotificationConfig
import po.misc.functions.registries.NotifierRegistry
import po.misc.functions.registries.TaggedRegistry
import po.misc.types.getOrThrow


fun <V: Any> Any.notifierRegistryOf(
    identifiedBy: Any,
    configBuilder: (NotificationConfig.()-> Unit)? = null
): NotifierRegistry<V> {

    val registry = when(identifiedBy){
        is Enum<*>->{
            NotifierRegistry<V>(this, identifiedBy.name)
        }
        else -> {
            NotifierRegistry<V>(this, identifiedBy.toString())
        }
    }
    configBuilder?.invoke(registry.notifierConfig)
    return registry
}

fun <E: Enum<E>, V: Any> CTX.subscribe(
    registry: NotifierRegistry<V>,
    callback: (V) -> Unit
){
    registry.subscribe(this, callback)
}

fun <V: Any> CTX.require(
    registry: NotifierRegistry<V>,
    callback: (V) -> Unit){

    registry.require(identity.kClass,  identity.numericId, callback)
}