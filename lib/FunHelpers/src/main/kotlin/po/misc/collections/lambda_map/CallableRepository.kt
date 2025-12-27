package po.misc.collections.lambda_map

import po.misc.types.token.TokenizedResolver
import po.misc.types.token.TypeToken
import po.misc.types.token.filterTokenized


interface CallableStorage<T, V>: TokenizedResolver<T, V>{
    override val receiverType: TypeToken<T>
    override val valueType: TypeToken<V>
    override val typeToken: TypeToken<T> get() = receiverType
    val hasReadOnlyProperty: Boolean
    val hasResolver: Boolean
    val callables: List<ReceiverCallable<T, V>>

    fun hasCollection(key: CallableDescriptor.CallableKey): Boolean
    fun add(callable: ReceiverCallable<T, V>): Boolean
    fun addAll(callables: List<ReceiverCallable<T,V>>): Boolean
    fun applyCallables(callableStorage: CallableStorage<T, V>)

    operator fun get(key: CallableDescriptor.CallableKey): ReceiverCallable<T, V>?
}

class CallableRepository<T, V>(
    override val receiverType: TypeToken<T>,
    override val valueType: TypeToken<V>,
):CallableStorage<T, V> {

    internal val listBacking = mutableListOf<ReceiverCallable<T, V>>()
    override val callables: List<ReceiverCallable<T, V>> get() = listBacking
    override val hasReadOnlyProperty: Boolean get() = listBacking.any { it is PropertyCallable<T, V> }
    override val hasResolver: Boolean get() = listBacking.any { it is FunctionCallable<T, V> }
    override fun hasCollection(key: CallableDescriptor.CallableKey): Boolean =
        listBacking.any { it.callableKey == key && it.valueType.isCollection }

    override fun add(callable: ReceiverCallable<T, V>): Boolean {
        return listBacking.add(callable )
    }
    override fun addAll(callables: List<ReceiverCallable<T, V>>): Boolean {
        callables.forEach {
            add(it)
        }
        return true
    }
    override fun applyCallables(callableStorage: CallableStorage<T, V>) {
        callableStorage.callables.forEach{
            add(it)
        }
    }

    inline fun <reified C: ReceiverCallable<T, V>> getCallable():C?{
        return  callables.filterTokenized<C, T, V>(receiverType, valueType).firstOrNull()
    }

    override operator fun get(key: CallableDescriptor.CallableKey): ReceiverCallable<T, V>? =
        listBacking.firstOrNull { it.keyEquals(key) }
}