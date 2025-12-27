package po.misc.data.pretty_print.parts.loader

import po.misc.callbacks.context_signal.ContextSignal
import po.misc.callbacks.context_signal.contextSignalOf
import po.misc.callbacks.signal.Signal
import po.misc.callbacks.signal.SignalOptions
import po.misc.callbacks.signal.signalOf
import po.misc.collections.lambda_map.CallableDescriptor
import po.misc.collections.lambda_map.CallableRepository
import po.misc.collections.lambda_map.CallableStorage
import po.misc.collections.lambda_map.PropertyCallable
import po.misc.context.tracable.TraceableContext
import po.misc.data.PrettyPrint
import po.misc.data.TextBuilder
import po.misc.data.snapshot
import po.misc.data.styles.SpecialChars
import po.misc.exceptions.error
import po.misc.functions.NoResult
import po.misc.functions.Throwing
import po.misc.types.getOrThrow
import po.misc.types.k_class.simpleOrAnon
import po.misc.types.token.TypeToken
import po.misc.types.token.asListType
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

class DataLoader<T, V>(
    val hostName: String,
    override val receiverType: TypeToken<T>,
    val valueToken: TypeToken<V>,
    val storage: CallableStorage<T, V> = CallableRepository(receiverType, valueToken)
): TraceableContext, TextBuilder, CallableStorage<T, V> by storage {

    class DataLoaderData(
        private val loader: DataLoader<*, *>
    ): PrettyPrint
    {
        val name: String = "${loader.hostName} snapshot"
        val hasProperty: String get() = loader.hasReadOnlyProperty.toFormatted()
        val hasListProperty: String = loader.hasListProperty.toFormatted()
        val hasProvider: String = loader.hasProvider.toFormatted()
        val hasListProvider: String = loader.hasListProvider.toFormatted()
        val hostResolvedSignalText: String get() = loader.hostResolved.info().formattedString
        val snapshot: String = snapshot().joinToString(SpecialChars.NEW_LINE)
        override val formattedString: String get() = buildString {
           appendLine("$name snapshot")
           append(snapshot)
        }
        override fun toString(): String = "ReceiverLoaderData[$loader]"
    }

    constructor(
        hostName: String,
        dataProvider: DataProvider<T, V>
    ):this(hostName, dataProvider.receiverType, dataProvider.valueType){
        storage.applyCallables(dataProvider)
    }

    val propertyReady : Signal<KProperty<*>, Unit> = signalOf()
    val dataSourceReady : Signal<CallableStorage<T, V>, Unit> = signalOf()
    val valueResolved: ContextSignal<V, Unit, Unit> = contextSignalOf(valueToken)
    val listValueResolved: ContextSignal<List<V>, Unit, Unit> = contextSignalOf(valueToken.asListType())
    val hostResolved: Signal<T, Unit> = signalOf(typeToken, NoResult, SignalOptions("DataLoader of $hostName", this))
    val canResolve: Boolean get() = hasReadOnlyProperty || hasResolver || hasProvider

    internal var listPropertyBacking: KProperty1<T, List<V>>? = null
    val listProperty: KProperty1<T, List<V>> get() {
        return listPropertyBacking.getOrThrow(KProperty1::class)
    }
    val hasListProperty: Boolean get() = listPropertyBacking != null

    internal var providerBacking: (() -> V)? = null
    val provider: () -> V get() {
        return providerBacking.getOrThrow(this)
    }
    val hasProvider: Boolean get() = providerBacking != null

    internal var listProviderBacking: (() ->  List<V>)? = null
    val listProvider: () ->  List<V> get() {
        return listProviderBacking.getOrThrow(this)
    }
    val hasListProvider: Boolean get() = listProviderBacking != null

    internal var resolverBacking: ((T) -> V)? = null
    val resolver: (T) -> V get() {
        return resolverBacking.getOrThrow(this)
    }

    val resolved: Signal<DataLoader<T, V>, Unit> = signalOf(NoResult)

    var lastValue: V? = null
        private set

    override fun applyCallables(callableStorage: CallableStorage<T, V>){
        storage.addAll(callableStorage.callables)
        dataSourceReady.trigger(this, callableStorage)
    }
    fun notifyResolved(receiver:T){
        hostResolved.trigger(receiver)
    }

    fun setProperty(property: KProperty1<T, V>){
        storage.add(PropertyCallable(receiverType, valueToken,  property))
        propertyReady.trigger(property)
    }
    @JvmName("setPropertyList")
    fun setProperty(property: KProperty1<T, List<V>>){
        listPropertyBacking = property
        propertyReady.trigger(property)
    }
    fun setProvider(provider: ()-> V){
        providerBacking = provider
    }
    fun setListProvider(provider: ()->  List<V>){
        listProviderBacking = provider
    }
    fun valueResolved(value: V){
        valueResolved.trigger(value)
    }

    fun getValue():V? = lastValue
    fun getValue(throwing: Throwing):V{
        val value = lastValue
        if(value != null){
            return value
        }
        val msg = "Unable to get last resolved value"
        error(msg)
    }

    fun resolveValue():V?{
        if(hasProvider){
            val value = provider.invoke()
            valueResolved(value)
            return value
        }
        return null
    }
    fun resolveValue(receiver:T):V?{
        val value =  storage[CallableDescriptor.CallableKey.ReadOnlyProperty]?.call(receiver)
        if(value != null){
            return value
        }
        if(hasProvider){
            val value =  provider.invoke()
            valueResolved(value)
            return value
        }
        if(hasResolver){
            val value =  resolver.invoke(receiver)
            valueResolved(value)
            return value
        }
        return null
    }
    fun resolveValue(receiver:T, throwing: Throwing):V{
        val value = resolveValue(receiver)
        if(value != null){
            return value
        }
        val message = "Impossible to resolve value of receiver type ${receiver!!::class.simpleOrAnon}"
        error(message)
    }
    fun resolveValue(throwing: Throwing):V{
        val value = resolveValue()
        if(value != null){
            return value
        }
        val message = "Impossible to resolve by provider. HasProvider: $hasProvider"
        error(message, DataLoaderData(this))
    }

    fun resolveList():List<V>{
        val result = mutableListOf<V>()
        if(hasListProvider){
            result.addAll(listProvider.invoke())
        }
        if(hasProvider){
            result.add(provider.invoke())
        }
        return result
    }
    fun resolveList(receiver:T):List<V>{
        return  storage.callables.map { it.call(receiver) }
    }
    fun resolveList(receiver:T, throwing: Throwing):List<V>{
        val result =  storage.callables.map { it.call(receiver) }
        if(result.isEmpty() && ! canResolve){
            val message = "Impossible to resolve value of receiver type ${receiver!!::class.simpleOrAnon}"
            error(message, DataLoaderData(this))
        }
        return result
    }
    fun resolveListOrNull(receiver:T):List<V>?{
        val result =  storage.callables.map { it.call(receiver) }
        if(result.isEmpty()) return null
        return result
    }
    fun resolveList(receiverList: List<T>):List<V>{
        val result = mutableListOf<V>()
        receiverList.forEach {
            result.addAll(resolveList(it))
        }
        return result
    }

    fun createDataProvider(): DataProvider<T, V>{
       return DataProvider(typeToken, valueToken).also {
            if(hasListProperty){
                it.resolveListTypeProperty(listProperty)
            }
        }
    }

    fun info(): DataLoaderData {
        return DataLoaderData(this)
    }
    fun copy():DataLoader<T, V>{
        return DataLoader(hostName, typeToken, valueToken).also {
            it.providerBacking = providerBacking
            it.listPropertyBacking = listPropertyBacking
            it.listProviderBacking = listProviderBacking
        }
    }
    override fun equals(other: Any?): Boolean {
        if(other !is DataLoader<*, *>) return false
        if(other.hostName != hostName) return false
        if(other.typeToken != typeToken) return false
        if(other.valueToken != valueToken) return false
        if(other.providerBacking != providerBacking) return false
        if(other.listPropertyBacking != listPropertyBacking) return false
        if(other.listProviderBacking != listProviderBacking) return false
        return true
    }
    override fun hashCode(): Int {
        var result = hostName.hashCode()
        result = 31 * result + typeToken.hashCode()
        result = 31 * result + valueToken.hashCode()
        result = 31 * result + providerBacking.hashCode()
        result = 31 * result + listPropertyBacking.hashCode()
        result = 31 * result + listProviderBacking.hashCode()
        return result
    }
}