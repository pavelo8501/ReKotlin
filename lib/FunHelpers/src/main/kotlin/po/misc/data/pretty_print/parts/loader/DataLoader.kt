package po.misc.data.pretty_print.parts.loader


import po.misc.callbacks.callable.CallableCollection
import po.misc.callbacks.callable.ProviderCallable
import po.misc.callbacks.context_signal.ContextSignal
import po.misc.callbacks.context_signal.contextSignalOf
import po.misc.callbacks.signal.Signal
import po.misc.callbacks.signal.SignalOptions
import po.misc.callbacks.signal.signalOf
import po.misc.callbacks.callable.CallableRepositoryBase
import po.misc.callbacks.callable.CallableRepositoryHub
import po.misc.callbacks.callable.CallableStorage
import po.misc.callbacks.callable.ReceiverCallable
import po.misc.context.tracable.TraceableContext
import po.misc.data.PrettyPrint
import po.misc.data.TextBuilder
import po.misc.data.snapshot
import po.misc.data.styles.SpecialChars
import po.misc.functions.CallableKey
import po.misc.functions.NoResult
import po.misc.types.token.TypeToken
import po.misc.types.token.asElementType
import po.misc.types.token.asListType
import kotlin.reflect.KProperty

class DataLoader<S, T>(
    val hostName: String,
    override val sourceType: TypeToken<S>,
    override val receiverType: TypeToken<T>,
): TraceableContext, TextBuilder, CallableRepositoryHub<S, T> {



    class DataLoaderData(
        private val loader: DataLoader<*, *>
    ): PrettyPrint {
        val name: String = "${loader.hostName} snapshot"
        val hasProperty: String get() = loader.elementRepository.hasProperty.toFormatted()
        val hasProvider: String = loader.elementRepository.hasProvider.toFormatted()
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
        listProvider: ListProvider<S, T>
    ):this(hostName, listProvider.sourceType, listProvider.receiverType.asElementType()){
        listRepository.apply(listProvider)
    }
    constructor(
        hostName: String,
        dataProvider: CallableRepositoryBase<S, T>
    ):this(hostName, dataProvider.sourceType, dataProvider.receiverType){
        elementRepository.apply(dataProvider)
    }

    override val elementRepository: ElementProvider<S, T> = ElementProvider(sourceType, receiverType)
    override val listRepository: ListProvider<S, T> = ListProvider<S, T>(sourceType, receiverType.asListType())

    val propertyReady : Signal<KProperty<*>, Unit> = signalOf()
    val dataSourceReady : Signal<CallableStorage<S, T>, Unit> = signalOf()
    val valueResolved: ContextSignal<T, Unit, Unit> = contextSignalOf(receiverType)
    val hostResolved: Signal<S, Unit> = signalOf(sourceType, NoResult, SignalOptions("DataLoader of $hostName", this))
    val resolved: Signal<DataLoader<S, T>, Unit> = signalOf(NoResult)

    private var lastValue: T? = null

    fun add(callable: ReceiverCallable<S, T>): Boolean = elementRepository.add(callable)
    @JvmName("addListTypeCallable")
    fun add(listCallable:ReceiverCallable<S, List<T>>): Boolean = listRepository.add(listCallable)

    fun apply(elementCollection: CallableCollection<S, T>): DataLoader<S, T>{
        elementRepository.apply(elementCollection)
        dataSourceReady.trigger(this, elementRepository)
        return this
    }
    @JvmName("applyListTypeCallableStorage")
    fun apply(callableStorage: CallableCollection<S, List<T>>): DataLoader<S, T>{
        listRepository.apply(callableStorage)
        return this
    }
    fun apply(listProvider: ListProvider<S, T>): DataLoader<S, T>{
        listRepository.apply(listProvider)
        dataSourceReady.trigger(this, elementRepository)
        return this
    }
    fun apply(provider: ElementProvider<S, T>): DataLoader<S,T> {
        elementRepository.apply(provider)
        return this
    }
    fun apply(dataLoader: DataLoader<S, T>): DataLoader<S, T> {
        elementRepository.apply(dataLoader.elementRepository)
        listRepository.apply(dataLoader.listRepository)
        return this
    }
    fun notifyResolved(receiver:S){
        hostResolved.trigger(receiver)
    }
    fun valueResolved(value: T){
        valueResolved.trigger(value)
    }
    fun resolveValue():T?{
        return elementRepository.getCallable<ProviderCallable<S, T>>()?.call()
    }
    fun resolveValue(receiver:S):T?{
        val valueByProperty = mutableListOf<T>()
        elementRepository[CallableKey.Property]?.let {callable->
            val values = callable.call(receiver)
            valueByProperty.add(values)
        }
        if(valueByProperty.isNotEmpty()){
           return valueByProperty.firstOrNull()
        }
        val valueByResolver =  elementRepository[CallableKey.Resolver]?.call(receiver)
        if(valueByResolver != null){
            valueResolved(valueByResolver)
            return valueByResolver
        }
        val valueByProvider = elementRepository.getCallable<ProviderCallable<S, T>>()?.call()
        if(valueByProvider != null){
            valueResolved(valueByProvider)
            return valueByProvider
        }
        return null
    }
    fun resolveValue(receiver:S, failureAction:(DataLoaderData)-> Nothing):T{
        val value = resolveValue(receiver)
        if(value != null){
            return value
        }
        failureAction.invoke(DataLoaderData(this))
    }
    fun resolveList():List<T>{
        val result = mutableListOf<T>()
        if(listRepository.canLoad){
           val values = listRepository.getCallable<ProviderCallable<S, List<T>>>()?.call()
           if(values?.isEmpty()?: false){
               result.addAll(values)
           }
        }
        return result
    }
    fun resolveList(receiver:S):List<T>{
        val values =  resolveAll(receiver)
        return values
    }
    fun resolveList(receiverList: List<S>):List<T>{
        val result = mutableListOf<T>()
        receiverList.forEach {
            result.addAll(resolveList(it))
        }
        return result
    }
    fun info(): DataLoaderData {
        return DataLoaderData(this)
    }
    fun copy():DataLoader<S, T>{
        val loaderCopy = DataLoader(hostName, sourceType, receiverType)
        loaderCopy.apply(elementRepository)
        loaderCopy.apply(listRepository)
        return loaderCopy
    }
    override fun equals(other: Any?): Boolean {
        if(other !is DataLoader<*, *>) return false
        if(other.hostName != hostName) return false
        if(other.sourceType != sourceType) return false
        if(other.receiverType != receiverType) return false
        return true
    }
    override fun hashCode(): Int {
        var result = hostName.hashCode()
        result = 31 * result + sourceType.hashCode()
        result = 31 * result + receiverType.hashCode()
        return result
    }
}