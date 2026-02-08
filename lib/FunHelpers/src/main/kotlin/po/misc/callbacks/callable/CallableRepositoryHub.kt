package po.misc.callbacks.callable

import po.misc.data.pretty_print.parts.loader.ElementProvider
import po.misc.data.pretty_print.parts.loader.ListProvider
import po.misc.functions.CallableKey
import po.misc.types.token.TokenizedResolver
import po.misc.types.token.TypeToken
import po.misc.types.token.asListType

interface CallableRepositoryHub<T, V> : TokenizedResolver<T, V> {

    val elementRepository : ElementProvider<T, V>
    val listRepository : ListProvider<T, V>
    override val sourceType: TypeToken<T> get() =  elementRepository.sourceType
    override val receiverType: TypeToken<V> get() =  elementRepository.receiverType

    val hasProperty:Boolean get() = elementRepository.hasProperty || listRepository.hasProperty
    val canResolve:Boolean get() = elementRepository.canLoad || listRepository.canLoad

    fun resolveAll(receiver: T): List<V>{
        val result = mutableListOf<V>()
        listRepository.callableList.forEach {
           val values  =  it.call(receiver)
           result.addAll(values)
        }
        elementRepository.callableList.forEach {
            val values  =  it.call(receiver)
            result.add(values)
        }
        return result
    }

    operator fun get(key: CallableKey):ReceiverCallable<T, *>?{
        return  elementRepository[key]?:run {
            listRepository[key]
        }
    }
    fun clearRepositories(){
        elementRepository.clear()
        listRepository.clear()
    }
}