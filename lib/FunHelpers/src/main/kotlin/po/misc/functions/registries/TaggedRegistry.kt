package po.misc.functions.registries

import po.misc.context.CTX
import po.misc.data.logging.LogEmitter
import po.misc.data.processors.SeverityLevel
import po.misc.functions.containers.Notifier
import po.misc.functions.models.NotificationConfig
import po.misc.functions.registries.models.TaggedSubscriber
import kotlin.collections.set
import kotlin.reflect.KClass


class TaggedRegistry<E: Enum<E>, V: Any>(
   val tag:E
): LogEmitter {

    private val noLambdaMsg = "$identifiedAs Unable to trigger. Lambda is null"
    private val subscriptionOverwrittenMsg = "$identifiedAs Lambda overwritten"

    private var notifierConfig: NotificationConfig = NotificationConfig()

    private val notifiers: MutableMap<RegistryKey, Notifier<V>> = mutableMapOf()
    private var owningContext: CTX? = null

    val subscriptionsCount: Int get() = notifiers.size
    val identifiedAs: String  get() = "TaggedRegistry<${tag.name}>"

    private fun notifyNoLambda(){
        if(notifierConfig.warnNoSubscriber){
            owningContext?.notify(noLambdaMsg, SeverityLevel.WARNING)?:run {
                notify(noLambdaMsg, SeverityLevel.WARNING)
            }
        }
    }
    private fun notifyLambdaOverwritten(){
        if(notifierConfig.warnSubscriptionOverwritten){
            owningContext?.notify(subscriptionOverwrittenMsg, SeverityLevel.WARNING)?:run {
                notify(subscriptionOverwrittenMsg, SeverityLevel.WARNING)
            }
        }
    }

    private fun createKey(kClass: KClass<*>, requireOnce: Boolean = false): TaggedSubscriber<E>{
       return TaggedSubscriber(tag, kClass, requireOnce)
    }

    internal fun setContext(context: CTX):TaggedRegistry<E, V>{
        owningContext = context
        return this
    }


    fun provideConfig(config: NotificationConfig){
        notifierConfig = config
    }

    fun trigger(value: V) {
        if (notifiers.isEmpty()) {
            notifyNoLambda()
        } else {
            val toRemove = mutableListOf<RegistryKey>()
            notifiers.forEach { (key, notifier) ->
                notifier.trigger(value)
                if (key.requireOnce) {
                    toRemove += key
                }
            }
            toRemove.forEach(notifiers::remove)
        }
    }

    fun trigger(kClass: KClass<*>, id: Long, value: V) {
        val key = createKey(kClass).setID(id)
        val notifier = notifiers[key]
        if (notifier != null) {
            if (key.requireOnce) {
                notifiers.remove(key)
            }
            notifier.trigger(value)
        } else {
            notifyNoLambda()
        }
    }

    fun trigger(kClass: KClass<*>, value: V) {
        val probe = TaggedSubscriber(tag, kClass, false).setID(0L)
        val foundKey = notifiers.keys.firstOrNull { it.matchesWildcard(probe) }
        foundKey?.let { notifiers[it]?.trigger(value) }?:run {
            notifyNoLambda()
        }
    }

    fun require(kClass: KClass<*>, callback: (V) -> Unit){
        val subscriptionKey = createKey(kClass, true)
        if(notifiers.containsKey(subscriptionKey)){
            notifyLambdaOverwritten()
        }
        notifiers[subscriptionKey] = Notifier(callback)
    }

    fun require(kClass: KClass<*>, id: Long, callback: (V) -> Unit){
        val subscriptionKey = createKey(kClass, true).setID(id)
        if(notifiers.containsKey(subscriptionKey)){
            notifyLambdaOverwritten()
        }
        notifiers[subscriptionKey] = Notifier(callback)
    }

    fun subscribe(kClass: KClass<*>, callback: (V) -> Unit){
        val subscriptionKey = createKey(kClass)
        if(notifiers.containsKey(subscriptionKey)){
            notifyLambdaOverwritten()
        }
        notifiers[subscriptionKey] = Notifier(callback)
    }

    fun subscribe(kClass: KClass<*>, id: Long, callback: (V) -> Unit){
        val subscriptionKey = createKey(kClass).setID(id)

        if(notifiers.containsKey(subscriptionKey)){
            notifyLambdaOverwritten()
        }
        notifiers[subscriptionKey] = Notifier(callback)
    }
}

fun <E: Enum<E>, V: Any> Any.taggedRegistryOf(
    tag: E
): TaggedRegistry<E, V>{
    val notifier = TaggedRegistry<E, V>(tag)
    if(this is CTX){
        notifier.setContext(this)
    }
    return notifier
}

fun <E: Enum<E>, V: Any> CTX.subscribe(registry: TaggedRegistry<E, V>,  callback: (V) -> Unit){
    registry.setContext(this).subscribe(identity.kClass, identity.numericId, callback)
}

fun <E: Enum<E>, V: Any> CTX.require(registry: TaggedRegistry<E, V>,  callback: (V) -> Unit){
    registry.setContext(this).require(this.identity.kClass,  identity.numericId, callback)
}



