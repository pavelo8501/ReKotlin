package po.misc.functions.registries.builders

import po.misc.context.CTX
import po.misc.functions.models.NotificationConfig
import po.misc.functions.registries.NotifierRegistry


fun <V: Any> Any.notifierRegistryOf(
    identifiedBy: Any? = null,
    configBuilder: (NotificationConfig.()-> Unit)? = null
): NotifierRegistry<V> {

    val identified = identifiedBy?:this

    val registry = when(identified){
        is Enum<*>->{
            NotifierRegistry<V>(this, identified.name)
        }
        else -> {
            NotifierRegistry<V>(this, identified.toString())
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

    registry.require(identity.typeData.kClass,  identity.numericId, callback)
}