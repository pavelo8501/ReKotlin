package po.misc.functions.registries

import po.misc.context.CTX
import po.misc.exceptions.ManagedException
import po.misc.functions.models.NotificationConfig
import po.misc.types.TypeData
import po.misc.types.getOrManaged
import po.misc.types.getOrThrow
import kotlin.reflect.KClass

inline fun <reified E: Enum<E>, V: Any> Any.taggedRegistryOf(
    tag: E? = null
): TaggedRegistry<E, V>{
    val notifier = TaggedRegistry<E, V>(E::class.java, tag)
    if(this is CTX){
        notifier.setContext(this)
    }
    return notifier
}

inline fun <reified E: Enum<E>, V: Any> Any.taggedRegistryOf(
    tag: E? = null,
    configBuilder: NotificationConfig.()-> Unit
): TaggedRegistry<E, V>{
    val notifier = TaggedRegistry<E, V>(E::class.java, tag)
    if(this is CTX){
        notifier.setContext(this)
    }
    notifier.notifierConfig.configBuilder()
    return notifier
}


fun <E: Enum<E>, V: Any> CTX.subscribe(registry: TaggedRegistry<E, V>,  callback: (V) -> Unit) {

    val exception =  ManagedException("If no key provided registry must have been configured with default key")
    val key = registry.presetKey.getOrThrow(registry.tagClass.kotlin, this) {exception }
    registry.setContext(this).subscribe(key, identity.kClass, identity.numericId, callback)
}

fun <E: Enum<E>, V: Any> CTX.require(registry: TaggedRegistry<E, V>,  callback: (V) -> Unit){

    val exception =  ManagedException("If no key provided registry must have been configured with default key")
    val key = registry.presetKey.getOrThrow(registry.tagClass.kotlin, this) { exception }

    registry.setContext(this).require(key, this.identity.kClass,  identity.numericId, callback)
}

fun <E: Enum<E>, V: Any> CTX.subscribe(tag:E, registry: TaggedRegistry<E, V>,  callback: (V) -> Unit){
    registry.setContext(this).subscribe(tag, identity.kClass, identity.numericId, callback)
}

fun <E: Enum<E>, V: Any> CTX.require(tag:E, registry: TaggedRegistry<E, V>,  callback: (V) -> Unit){
    registry.setContext(this).require(tag, this.identity.kClass,  identity.numericId, callback)
}


inline fun <reified S: Any, reified E: Enum<E>, V: Any> S.emitterAwareRegistryOf(

): EmitterAwareRegistry<S, E, V>{

    val registry = if(this is CTX){
        @Suppress("UNCHECKED_CAST")
        val registry = EmitterAwareRegistry<S, E, V>(identity.typeData as TypeData<S>, E::class.java)
        registry.setContext(this)
        registry
    }else{
        EmitterAwareRegistry<S, E, V>(TypeData.create<S>(), E::class.java)
    }
    return registry
}


inline fun <S: Any, reified E: Enum<E>, V: Any> buildRegistry(
  emitterTypeData: TypeData<S>
): EmitterAwareRegistry<S, E, V>{

    val registry = EmitterAwareRegistry<S, E, V>(emitterTypeData, E::class.java)


    return registry
}
