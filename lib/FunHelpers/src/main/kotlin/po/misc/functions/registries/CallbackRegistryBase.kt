package po.misc.functions.registries

import po.misc.context.CTX
import po.misc.data.logging.LogEmitter
import po.misc.data.processors.SeverityLevel
import po.misc.functions.containers.Notifier
import po.misc.functions.models.NotificationConfig
import po.misc.functions.registries.models.SimpleSubscriber
import po.misc.functions.registries.models.TaggedSubscriber
import po.misc.types.TypeData
import po.misc.types.castListOrManaged
import po.misc.types.helpers.simpleOrNan
import po.misc.types.safeCast
import kotlin.collections.set
import kotlin.reflect.KClass


sealed class CallbackRegistryBase<V: Any>(
    protected val identifiedAs: String
): LogEmitter{

    @PublishedApi
    internal var notifierConfig: NotificationConfig = NotificationConfig()

    protected val noLambdaMsg: ( name:String)-> String = {name->
        "$name Unable to trigger. Lambda is null"
    }
    protected val subscriptionOverwrittenMsg: ( name:String)-> String = {name->
        "$name Lambda overwritten"
    }
    protected val subscriptionFailedMsg: (subscriberName: String, emitterName:String)-> String = {subscriberName, emitterName->
        "No subscription made for $subscriberName. While trying to subscribe for $emitterName emissions"
    }
    protected var owningContext: CTX? = null

    protected val notifiers: MutableMap<RegistryKey, Notifier<V>> = mutableMapOf()
    val subscriptionsCount: Int get() = notifiers.size
    val requireOnceCount: Int get() = notifiers.keys.count { it.requireOnce }
    val permanentCount: Int get() = notifiers.keys.count { !it.requireOnce }

    protected fun notifyNoLambda(){
        if(notifierConfig.warnNoSubscriber){
            owningContext?.notify(noLambdaMsg(identifiedAs), SeverityLevel.WARNING)?:run {
                notify(noLambdaMsg(identifiedAs), SeverityLevel.WARNING)
            }
        }
    }
    protected fun notifyLambdaOverwritten(){
        if(notifierConfig.warnSubscriptionOverwritten){
            owningContext?.notify(subscriptionOverwrittenMsg(identifiedAs), SeverityLevel.WARNING)?:run {
                notify(subscriptionOverwrittenMsg(identifiedAs), SeverityLevel.WARNING)
            }
        }
    }


    fun provideConfig(config: NotificationConfig):CallbackRegistryBase<V>{
        notifierConfig = config
        return this
    }

    protected fun trigger(key: RegistryKey, value: V) {
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

    fun clear() {
        notifiers.clear()
    }
}

class NotifierRegistry<V: Any>(

):CallbackRegistryBase<V>("NotifierRegistry"){


    private fun createKey(kClass: KClass<*>, requireOnce: Boolean = false): SimpleSubscriber{
        return SimpleSubscriber(kClass, requireOnce)
    }

    fun subscribe(subscriber: Any, subscriberId: Long, callback:(V)-> Unit){
        val key = createKey(subscriber::class)
        key.setID(subscriberId)
        if(notifiers.containsKey(key)){
            notifyLambdaOverwritten()
        }
        notifiers[key] = Notifier(callback)
    }

    fun subscribe(subscriber: Any, callback:(V)-> Unit){
        val key = createKey(subscriber::class)
        if(notifiers.containsKey(key)){
            notifyLambdaOverwritten()
        }
        notifiers[key] = Notifier(callback)
    }

    fun trigger(kClass: KClass<*>, value: V) {
        val key = createKey(kClass)
        trigger(key, value)
    }

    fun trigger(kClass: KClass<*>, id: Long, value: V) {
        val key = createKey(kClass).setID(id)
        trigger(key, value)
    }

}

class TaggedRegistry<E: Enum<E>, V: Any>(
    internal val tagClass: Class<E>,
    internal val presetKey: E? = null
): CallbackRegistryBase<V>("TaggedRegistry<${tagClass.name}>") {

    private fun createKey(tag:E,  kClass: KClass<*>, requireOnce: Boolean = false): TaggedSubscriber<E>{
       return TaggedSubscriber(tag, kClass, requireOnce)
    }

    @PublishedApi
    internal fun setContext(context: CTX):TaggedRegistry<E, V>{
        owningContext = context
        return this
    }

    fun trigger(tag:E, kClass: KClass<*>, id: Long, value: V) {
        val key = createKey(tag, kClass).setID(id)
        trigger(key, value)
    }

    fun trigger(kClass: KClass<*>, value: V) {
        val probe = SimpleSubscriber(kClass, false).setID(0L)
        val foundKey = notifiers.keys.firstOrNull { it.matchesWildcard(probe) }
        foundKey?.let { notifiers[it]?.trigger(value) }?:run {
            notifyNoLambda()
        }
    }

    fun trigger(tag:E, value: V) {
        val keys = notifiers.keys.filterIsInstance<TaggedSubscriber<E>>()
        keys.filter { it.enumTag ==  tag}.forEach {foundKey->
            trigger(foundKey, value)
        }
    }

    fun require(tag:E, kClass: KClass<*>, callback: (V) -> Unit){
        val subscriptionKey = createKey(tag, kClass, true)
        if(notifiers.containsKey(subscriptionKey)){
            notifyLambdaOverwritten()
        }
        notifiers[subscriptionKey] = Notifier(callback)
    }

    fun require(tag: E, kClass: KClass<*>, id: Long, callback: (V) -> Unit){
        val subscriptionKey = createKey(tag, kClass, true).setID(id)
        if(notifiers.containsKey(subscriptionKey)){
            notifyLambdaOverwritten()
        }
        notifiers[subscriptionKey] = Notifier(callback)
    }

    fun subscribe(tag: E,  kClass: KClass<*>, callback: (V) -> Unit){
        val subscriptionKey = createKey(tag, kClass)
        if(notifiers.containsKey(subscriptionKey)){
            notifyLambdaOverwritten()
        }
        notifiers[subscriptionKey] = Notifier(callback)
    }

    fun subscribe(tag: E, kClass: KClass<*>, id: Long, callback: (V) -> Unit){
        val subscriptionKey = createKey(tag, kClass).setID(id)

        if(notifiers.containsKey(subscriptionKey)){
            notifyLambdaOverwritten()
        }
        notifiers[subscriptionKey] = Notifier(callback)
    }
}

class EmitterAwareRegistry<S: Any, E: Enum<E>, V: Any>(
    val emitterClass: TypeData<S>,
    val tagClass: Class<E>
): CallbackRegistryBase<V>("EmitterAwareRegistry<${tagClass.name}>")
{
    private fun createKey(tag:E, subscriberClass: KClass<*>, requireOnce: Boolean = false): TaggedSubscriber<E>{
        return TaggedSubscriber(tag, subscriberClass, requireOnce)
    }

    private fun notifySubscriptionFailed(subscriber: String, targetClass: String){
        if(notifierConfig.warnSubscriptionFailed){
            owningContext?.notify(subscriptionFailedMsg(subscriber, targetClass), SeverityLevel.WARNING)?:run {
                notify(subscriptionFailedMsg(subscriber, targetClass), SeverityLevel.WARNING)
            }
        }
    }

    @PublishedApi
    internal fun setContext(context: CTX):EmitterAwareRegistry<S, E, V>{
        owningContext = context
        return this
    }

    fun trigger(tag:E, subscriberClass: KClass<*>, id: Long, value: V) {
        val probe = TaggedSubscriber(tag, subscriberClass, false).setID(id)
        val foundKey = notifiers.keys.firstOrNull { it.matchesWildcard(probe) }
        if(foundKey != null){
            trigger(foundKey, value)
        }else{
            notifyNoLambda()
        }
    }

    fun trigger(tag:E, subscriberClass: KClass<*>,  value: V) {
        val probe = TaggedSubscriber(tag, subscriberClass, false).setID(0L)
        val foundKey = notifiers.keys.firstOrNull { it.matchesWildcard(probe) }
        if(foundKey != null){
            trigger(foundKey, value)
        }else{
            notifyNoLambda()
        }
    }

    fun trigger(tag:E, value: V) {
        val keys = notifiers.keys.filterIsInstance<TaggedSubscriber<E>>()
        keys.filter { it.enumTag ==  tag}.forEach {foundKey->
            trigger(foundKey, value)
        }
    }

    fun require(tag:E, subscriberClass: KClass<*>, callback: (V) -> Unit){
        val subscriptionKey = createKey(tag, subscriberClass, true)
        if(notifiers.containsKey(subscriptionKey)){
            notifyLambdaOverwritten()
        }
        notifiers[subscriptionKey] = Notifier(callback)
    }

    fun require(tag:E, subscriberClass: KClass<*>, id: Long, callback: (V) -> Unit){
        val subscriptionKey = createKey(tag, subscriberClass, true).setID(id)
        if(notifiers.containsKey(subscriptionKey)){
            notifyLambdaOverwritten()
        }
        notifiers[subscriptionKey] = Notifier(callback)
    }

    fun subscribe(tag:E, subscriberClass: KClass<*>, callback: (V) -> Unit){
        val subscriptionKey = createKey(tag, subscriberClass)
        if(notifiers.containsKey(subscriptionKey)){
            notifyLambdaOverwritten()
        }
        notifiers[subscriptionKey] = Notifier(callback)
    }

    fun trySubscribe(tag:E, subscriberClass: KClass<*>, targetClass: KClass<*>, callback: (V) -> Unit){
        if(targetClass == emitterClass.kClass){
            val subscriptionKey = createKey(tag, subscriberClass)
            if(notifiers.containsKey(subscriptionKey)){
                notifyLambdaOverwritten()
            }
            notifiers[subscriptionKey] = Notifier(callback)
        }else{
            notifySubscriptionFailed(subscriberClass::class.simpleOrNan(), targetClass.simpleOrNan())
        }
    }

    private fun proceedSubscription(bulkSubscriptions: EmitterSubscriptions<*>){
        val casted = bulkSubscriptions.safeCast<EmitterSubscriptions<V>>()
        if(casted != null){
            casted.subscriptions.forEach {subscription->
                @Suppress("UNCHECKED_CAST")
                val asTagged = subscription.first as TaggedSubscriber<E>

                if(asTagged.requireOnce){
                    require(asTagged.enumTag, asTagged.kClass, subscription.second)
                }else{
                    subscribe(asTagged.enumTag, asTagged.kClass, subscription.second)
                }
            }
        }else{
            notify("bulkSubscriptions cast failure", SeverityLevel.WARNING)
        }
    }

    fun trySubscribe(pack: SubscriptionPack<*>){
        when(pack){
            is EmitterSubscriptions->{
                if(pack.targetEmitter == emitterClass.kClass){
                    proceedSubscription(pack)
                }else{
                    notifySubscriptionFailed(pack.subscriber.simpleOrNan(), pack.targetEmitter.simpleOrNan())
                }
            }
        }
    }

}