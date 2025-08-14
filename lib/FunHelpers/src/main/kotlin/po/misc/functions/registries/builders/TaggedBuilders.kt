package po.misc.functions.registries.builders

import po.misc.context.CTX
import po.misc.exceptions.ManagedException
import po.misc.functions.models.NotificationConfig
import po.misc.functions.registries.TaggedRegistry
import po.misc.types.TypeData
import po.misc.types.getOrThrow


inline fun <reified E: Enum<E>, V: Any> Any.taggedRegistryOf(
    tag: E? = null
): TaggedRegistry<E, V> {
    val notifier = TaggedRegistry<E, V>(this, E::class.java, tag)
    return notifier
}

inline fun <reified E: Enum<E>, V: Any> Any.taggedRegistryOf(
    tag: E? = null,
    configBuilder: NotificationConfig.()-> Unit
): TaggedRegistry<E, V> {

    val notifier = TaggedRegistry<E, V>(this,  E::class.java, tag)
    notifier.notifierConfig.configBuilder()
    return notifier
}

//inline fun <reified E: Enum<E>, V: Any> taggedRegistryFor(
//    owner:Any,
//    tag: E? = null
//): TaggedRegistry<E, V> {
//    val notifier = TaggedRegistry<E, V>(owner, E::class.java, tag)
//    return notifier
//}
//
//inline fun <reified E: Enum<E>, V: Any> taggedRegistryFor(
//    owner:Any,
//    tag: E? = null,
//    configBuilder: NotificationConfig.()-> Unit
//): TaggedRegistry<E, V> {
//    val notifier = TaggedRegistry<E, V>(owner,  E::class.java, tag)
//    notifier.notifierConfig.configBuilder()
//    return notifier
//}

fun <E: Enum<E>, V: Any> CTX.subscribe(registry: TaggedRegistry<E, V>, callback: (V) -> Unit) {
    val exception =  ManagedException("If no key provided registry must have been configured with default key")
    val key = registry.presetKey.getOrThrow(registry.tagClass.kotlin, this) {exception }
    registry.subscribe(key, identity.kClass, identity.numericId, callback)
}

fun <E: Enum<E>, V: Any> CTX.require(registry: TaggedRegistry<E, V>, callback: (V) -> Unit){
    val exception =  ManagedException("If no key provided registry must have been configured with default key")
    val key = registry.presetKey.getOrThrow(registry.tagClass.kotlin, this) { exception }
    registry.require(key, this.identity.kClass,  identity.numericId, callback)
}

fun <E: Enum<E>, V: Any> CTX.subscribe(tag:E, registry: TaggedRegistry<E, V>, callback: (V) -> Unit){
    registry.subscribe(tag, identity.kClass, identity.numericId, callback)
}

fun <E: Enum<E>, V: Any> CTX.require(tag:E, registry: TaggedRegistry<E, V>, callback: (V) -> Unit){
    registry.require(tag, this.identity.kClass,  identity.numericId, callback)
}