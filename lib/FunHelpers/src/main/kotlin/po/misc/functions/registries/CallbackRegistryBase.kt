package po.misc.functions.registries

import po.misc.context.CTX
import po.misc.data.logging.LogEmitter
import po.misc.data.processors.SeverityLevel
import po.misc.functions.containers.DSLNotifier
import po.misc.functions.containers.LambdaUnit
import po.misc.functions.containers.Notifier
import po.misc.functions.models.NotificationConfig
import po.misc.functions.registries.models.SimpleSubscriber
import po.misc.functions.registries.models.TaggedSubscriber
import po.misc.types.k_class.simpleOrAnon
import po.misc.types.safeCast
import po.misc.types.token.TypeToken
import kotlin.collections.set
import kotlin.reflect.KClass


sealed class CallbackRegistryBase<V: Any, R: Any>(
    protected val name: String
): LogEmitter{

    @PublishedApi
    internal var notifierConfig: NotificationConfig = NotificationConfig()

   // protected var  emitterTypeBacking : TypeData<*>? = null
   // protected open val emitterType : TypeData<*>? = null
    //protected val emitterTypeBacking: BackingContainer<TypeData<*>> = backingContainerOf()

    protected abstract val emitterType : TypeToken<*>?

    protected val noLambdaMsg: ( name:String)-> String = {name->
        "$name Unable to trigger. Lambda is null"
    }
    protected val subscriptionOverwrittenMsg: ( name:String)-> String = {name->
        "$name Lambda overwritten"
    }
    protected val subscriptionFailedMsg: (String, String)-> String = {subscriberName, emitterName->
        "No subscription made for $subscriberName. While trying to subscribe for $emitterName emissions"
    }
    internal var owningContext: CTX? = null


    protected open val notifiers: MutableMap<RegistryKey, LambdaUnit<V, R>> = mutableMapOf()

    val subscriptionsCount: Int get() = notifiers.size
    val requireOnceCount: Int get() = notifiers.keys.count { it.requireOnce }
    val permanentCount: Int get() = notifiers.keys.count { !it.requireOnce }

    protected fun notifyNoLambda(){
        if(notifierConfig.warnNoSubscriber){
            owningContext?.notify(noLambdaMsg(name), SeverityLevel.WARNING)?:run {
                notify(noLambdaMsg(name), SeverityLevel.WARNING)
            }
        }
    }
    protected fun notifyLambdaOverwritten(){
        if(notifierConfig.warnSubscriptionOverwritten){
            owningContext?.notify(subscriptionOverwrittenMsg(name), SeverityLevel.WARNING)?:run {
                notify(subscriptionOverwrittenMsg(name), SeverityLevel.WARNING)
            }
        }
    }


    fun provideConfig(config: NotificationConfig):CallbackRegistryBase<V, *>{
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
    val emitter: Any,
    private val identifiedBy: String
):CallbackRegistryBase<V, Unit>("NotifierRegistry[$identifiedBy]"){

    override val emitterType: TypeToken<*>? by lazy {
        if(emitter is CTX){
            emitter.identity.typeData
        }else{
            null
        }
    }

    init {
        if(emitter is CTX){
            owningContext = emitter
        }
    }

    private fun createKey(kClass: KClass<*>, requireOnce: Boolean = false): SimpleSubscriber{
        return SimpleSubscriber(kClass, requireOnce)
    }

    fun require(subscriber: KClass<*>, callback: (V) -> Unit) {
        val subscriptionKey = createKey(subscriber, true)
        if (notifiers.containsKey(subscriptionKey)) {
            notifyLambdaOverwritten()
        }
        notifiers[subscriptionKey] = Notifier(callback)
    }

    fun require(subscriber: KClass<*>, id: Long, callback: (V) -> Unit) {
        val subscriptionKey = createKey(subscriber, true)
        subscriptionKey.setID(id)
        if (notifiers.containsKey(subscriptionKey)) {
            notifyLambdaOverwritten()
        }
        notifiers[subscriptionKey] = Notifier(callback)
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
        key.setID(notifiers.size + 1L)
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

class DSLRegistry<T: Any, P: Any>(
   val emitter: Any,
   val typeData: TypeToken<T>,
   val parameter:P
):CallbackRegistryBase<T, Unit>("NotifierRegistry"){

    override val emitterType: TypeToken<*>? by lazy {
        if(emitter is CTX){
            emitter.identity.typeData
        }else{
            null
        }
    }

    private fun createKey(kClass: KClass<*>, requireOnce: Boolean = false): SimpleSubscriber{
        return SimpleSubscriber(kClass, requireOnce)
    }

    fun require(subscriber: KClass<*>, callback: (T) -> Unit) {
        val subscriptionKey = createKey(subscriber, true)
        if (notifiers.containsKey(subscriptionKey)) {
            notifyLambdaOverwritten()
        }
        notifiers[subscriptionKey] = Notifier(callback)
    }

    fun require(subscriber: KClass<*>, id: Long, callback: (T) -> Unit) {
        val subscriptionKey = createKey(subscriber, true)
        subscriptionKey.setID(id)
        if (notifiers.containsKey(subscriptionKey)) {
            notifyLambdaOverwritten()
        }
        notifiers[subscriptionKey] = Notifier(callback)
    }

    fun subscribe(subscriber: Any, subscriberId: Long, callback: T.(P)-> Unit){
        val key = createKey(subscriber::class)
        key.setID(subscriberId)
        if(notifiers.containsKey(key)){
            notifyLambdaOverwritten()
        }
        notifiers[key] = DSLNotifier(typeData, parameter, callback)
    }

    fun subscribe(subscriber: Any, callback: T.(P)-> Unit){
        val key = createKey(subscriber::class)
        key.setID(notifiers.size + 1L)
        if(notifiers.containsKey(key)){
            notifyLambdaOverwritten()
        }
        notifiers[key] = DSLNotifier(typeData, parameter, callback)
    }

    fun trigger(kClass: KClass<*>, value: T) {
        val key = createKey(kClass)
        trigger(key, value)
    }

    fun trigger(kClass: KClass<*>, id: Long, value: T) {
        val key = createKey(kClass).setID(id)
        trigger(key, value)
    }
}

class TaggedRegistry<E: Enum<E>, V: Any>(
    val emitter: Any,
    internal val tagClass: Class<E>,
    internal val presetKey: E? = null
): CallbackRegistryBase<V, Unit>("TaggedRegistry<${tagClass.name}>") {


    var emitterTypeBacking: TypeToken<*>? = null

    override val emitterType: TypeToken<*>? by lazy {
        emitterTypeBacking?:run {
            if(emitter is CTX){
                emitter.identity.typeData
            }else{
                emitterTypeBacking
            }
        }
    }

    init {
        if(emitter is CTX){
            owningContext = emitter
        }
    }

    private fun createKey(tag: E, kClass: KClass<*>, requireOnce: Boolean = false): TaggedSubscriber<E> {
        return TaggedSubscriber(tag, kClass, requireOnce)
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

    private fun notifySubscriptionFailed(subscriber: String, targetClass: String){
        if(notifierConfig.warnSubscriptionFailed){
            owningContext?.notify(subscriptionFailedMsg(subscriber, targetClass), SeverityLevel.WARNING)?:run {
                notify(subscriptionFailedMsg(subscriber, targetClass), SeverityLevel.WARNING)
            }
        }
    }

    fun identifiedByType(typeData: TypeToken<*>):TaggedRegistry<E, V>{
        emitterTypeBacking = typeData
        return this
    }

    fun trigger(tag: E, kClass: KClass<*>, id: Long, value: V) {
        val key = createKey(tag, kClass).setID(id)
        trigger(key, value)
    }

    fun trigger(kClass: KClass<*>, value: V) {
        val probe = SimpleSubscriber(kClass, false).setID(0L)
        val foundKey = notifiers.keys.firstOrNull {
            it.matchesWildcard(probe)
        }
        foundKey?.let { notifiers[it]?.trigger(value) } ?: run {
            notifyNoLambda()
        }
    }

    fun trigger(tag: E, value: V) {
        val keys = notifiers.keys.filterIsInstance<TaggedSubscriber<E>>()
        keys.filter { it.enumTag == tag }.forEach { foundKey ->
            trigger(foundKey, value)
        }
    }

    fun require(tag: E, kClass: KClass<*>, callback: (V) -> Unit) {
        val subscriptionKey = createKey(tag, kClass, true)
        if (notifiers.containsKey(subscriptionKey)) {
            notifyLambdaOverwritten()
        }
        notifiers[subscriptionKey] = Notifier(callback)
    }

    fun require(tag: E, kClass: KClass<*>, id: Long, callback: (V) -> Unit) {
        val subscriptionKey = createKey(tag, kClass, true).setID(id)
        if (notifiers.containsKey(subscriptionKey)) {
            notifyLambdaOverwritten()
        }
        notifiers[subscriptionKey] = Notifier(callback)
    }

    fun subscribe(tag: E, kClass: KClass<*>, callback: (V) -> Unit) {
        val subscriptionKey = createKey(tag, kClass)
        if (notifiers.containsKey(subscriptionKey)) {
            notifyLambdaOverwritten()
        }
        notifiers[subscriptionKey] = Notifier(callback)
    }

    fun subscribe(tag: E, kClass: KClass<*>, id: Long, callback: (V) -> Unit) {
        val subscriptionKey = createKey(tag, kClass).setID(id)

        if (notifiers.containsKey(subscriptionKey)) {
            notifyLambdaOverwritten()
        }
        notifiers[subscriptionKey] = Notifier(callback)
    }

    fun trySubscribe(pack: SubscriptionPack<*>){
        when(pack){
            is EmitterSubscriptions->{
                if(pack.targetEmitter == emitterType){
                    proceedSubscription(pack)
                }else{
                    notifySubscriptionFailed(pack.subscriber.simpleOrAnon, pack.targetEmitter.typeName)
                }
            }
        }
    }

}
