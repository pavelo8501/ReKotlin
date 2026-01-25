package po.misc.callbacks.callable

import po.misc.collections.putOverwriting
import po.misc.data.styles.Colour
import po.misc.functions.CallableKey
import po.misc.interfaces.named.NamedComponent
import po.misc.types.token.CastOptions
import po.misc.types.token.TokenFactory
import po.misc.types.token.TokenizedResolver
import po.misc.types.token.TypeToken
import po.misc.types.token.filterTokenized
import kotlin.reflect.KProperty1



interface CallableCollection<T, R>{

    val parameterType: TypeToken<T>
    val resultType: TypeToken<R>
    val callableList : List<ReceiverCallable<T, R>>
}

interface CallableStorage<T, V>: TokenizedResolver<T, V>, CallableCollection<T, V>, NamedComponent{

    override val sourceType: TypeToken<T>
    override val receiverType: TypeToken<V>

    override val parameterType: TypeToken<T> get() = sourceType
    override val resultType: TypeToken<V> get() = receiverType

    val hasProperty: Boolean
    val hasResolver: Boolean
    val hasProvider: Boolean
    val callables: Map<CallableKey, ReceiverCallable<T, V>>
    override val callableList : List<ReceiverCallable<T, V>>

    fun add(callable: ReceiverCallable<T, V>): Boolean
    fun addAll(callables: List<ReceiverCallable<T,V>>): Boolean
    fun apply(callableStorage: CallableCollection<T, V>):CallableRepositoryBase<T, V>
    operator fun get(key: CallableKey): ReceiverCallable<T, V>?{
        val single = callableList.firstOrNull { it.callableKey == key }
        if(single!=null){
            return single
        }
        return null
    }
}

abstract class CallableRepositoryBase<T, V>(
    val  repositoryName : String,
    override val sourceType: TypeToken<T>,
    override val receiverType: TypeToken<V>,
): CallableStorage<T, V>, CallableCollection<T, V> {

    val callPriority: MutableList<CallableKey> = mutableListOf(CallableKey.Provider, CallableKey.Property, CallableKey.Resolver)

    internal val callablesMapBacking = mutableMapOf<CallableKey, ReceiverCallable<T, V>>()
    protected val storageData: String get() = buildString {
        append(repositoryName)
        append("[${CallableKey.Property}: $hasProperty ")
        append("${CallableKey.Resolver}: $hasResolver]")
        append("${CallableKey.Provider}: $hasProvider]")
    }

    override val callables : Map<CallableKey, ReceiverCallable<T, V>> get() = callablesMapBacking
    override val hasResolver: Boolean get() = callables[CallableKey.Resolver] != null
    override val hasProvider: Boolean get() = callables[CallableKey.Provider] != null
    override val hasProperty: Boolean get() = callables[CallableKey.Property] != null
    val canLoad: Boolean get() = hasProperty || hasProvider || hasResolver
    override val callableList : List<ReceiverCallable<T, V>> get() = callables.values.toList()
    val size : Int get() = callables.size

    override fun add(callable: ReceiverCallable<T, V>): Boolean {
        var newEntry = true
        callablesMapBacking.putOverwriting(callable.callableKey, callable){
            "Callable $it has been overwritten by by $callable".output(Colour.YellowBright)
            newEntry = true
        }
        return newEntry
    }
    fun addNotNull(callable: ReceiverCallable<T, V>?){
        callable?.let { add(it) }
    }
    override fun addAll(callables: List<ReceiverCallable<T, V>>): Boolean {
        callables.forEach {
            add(it)
        }
        return true
    }
    override fun apply(callableStorage: CallableCollection<T, V>):CallableRepositoryBase<T, V>{
        callableStorage.callableList.forEach{
            add(it)
        }
        return this
    }
    inline fun <reified C: ReceiverCallable<T, V>> getCallable(options: CastOptions = CastOptions()):C?{

        return callableList.filterTokenized<C, T, V>(sourceType, receiverType, options).firstOrNull()
    }
    open fun call(receiver:T):V{
        return callPriority.firstNotNullOf { callables[it] }.call(receiver)
    }
    fun clear(): Unit = callablesMapBacking.clear()

}

class CallableRepository<T, V>(
    callable:ReceiverCallable<T, V>,
):CallableRepositoryBase<T, V>("CallableRepository",  callable.sourceType, callable.receiverType) {
    init { add(callable) }
    override val name: String = "CallableRepository<${sourceType.typeName}, ${receiverType.typeName}>"

    override fun toString(): String = storageData
    companion object : TokenFactory {
        inline operator fun <reified T, reified V> invoke(
            property: KProperty1<T, V>,
        ): CallableRepository<T, V> {
            return CallableRepository(PropertyCallable<T, V>(property))
        }
    }
}


