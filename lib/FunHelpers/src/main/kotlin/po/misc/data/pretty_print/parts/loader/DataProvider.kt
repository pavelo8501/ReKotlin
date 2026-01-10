package po.misc.data.pretty_print.parts.loader

import po.misc.callbacks.callable.FunctionCallable
import po.misc.callbacks.callable.PropertyCallable
import po.misc.callbacks.callable.ProviderCallable
import po.misc.callbacks.callable.ReceiverCallable
import po.misc.callbacks.callable.CallableRepository
import po.misc.callbacks.callable.CallableRepositoryBase
import po.misc.callbacks.callable.CallableStorage
import po.misc.collections.lambda_map.toCallable
import po.misc.data.pretty_print.cells.KeyedCell.Companion.valueType
import po.misc.data.pretty_print.parts.loader.ListProvider
import po.misc.functions.CallableKey
import po.misc.reflection.displayName
import po.misc.types.castOrThrow
import po.misc.types.k_function.lambdaName
import po.misc.types.token.TokenFactory
import po.misc.types.token.TokenizedResolver
import po.misc.types.token.TypeProvider
import po.misc.types.token.TypeToken
import po.misc.types.token.asElementType
import po.misc.types.token.asListType
import po.misc.types.token.tokenOf
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class ElementProvider<T, V>(
    receiverType:TypeToken<T>,
    valueType:TypeToken<V>,
): CallableRepositoryBase<T, V>("Provider", receiverType, valueType) {
    constructor(callable: ReceiverCallable<T, V>): this(callable.sourceType, callable.receiverType ){
        add(callable)
    }
    override val name: String = storageData
    fun callAll(receiver:T):List<V> = callables.values.map { it.call(receiver) }

    fun copy(): ElementProvider<T, V>{
        val provider = ElementProvider(parameterType, resultType)
        provider.addAll(callableList)
        return provider
    }

    companion object : TokenFactory {
        inline operator fun <reified T, reified V> invoke(
            property: KProperty1<T, V>,
        ): ElementProvider<T, V> {
            return ElementProvider(PropertyCallable<T, V>(property))
        }

        inline operator fun <T, reified V> invoke(
            receiverType: TypeToken<T>,
            property: KProperty1<T, V>,
        ): ElementProvider<T, V> {
            return ElementProvider(PropertyCallable(receiverType, property))
        }

        inline operator fun <T, reified V> invoke(
            receiverType: TypeToken<T>,
            noinline function: Function1<T, V>,
        ): ElementProvider<T, V> {
            return ElementProvider(FunctionCallable<T, V>(receiverType, function))
        }

        inline operator fun <reified T, reified V> invoke(
            noinline function: Function1<T, V>,
        ): ElementProvider<T, V> {
            return ElementProvider(FunctionCallable<T, V>(function))
        }

        operator fun <T, V> invoke(
            receiverType: TypeToken<T>,
            valueType: TypeToken<V>,
            function: Function1<T, V>,
        ): ElementProvider<T, V> {
            return ElementProvider(FunctionCallable<T, V>(receiverType, valueType,  function))
        }
    }
}

class ListProvider<T, V>(
     receiverType:TypeToken<T>,
     valueType:TypeToken<List<V>>,
     callable: ReceiverCallable<T, List<V>>? = null
): CallableRepositoryBase<T, List<V>>("Provider", receiverType, valueType) {
    override val name: String = storageData
    init { addNotNull(callable) }
    override fun call(receiver:T):List<V> = callables.values.flatMap { it.call(receiver) }
    companion object : TokenFactory {
        inline operator fun <reified T, reified V> invoke(
            property: KProperty1<T, List<V>>,
            companion: Companion = ListProvider
        ): ListProvider<T, V> {
            return ListProvider(tokenOf<T>(), tokenOf<List<V>>(),  PropertyCallable(property))
        }
        inline operator fun <T, reified V> invoke(
            receiverType:TypeToken<T>,
            property: KProperty1<T, List<V>>,
        ): ListProvider<T, V> {
            return ListProvider(receiverType, tokenOf<List<V>>(),   PropertyCallable(receiverType,  property))
        }

        operator fun <T, V> invoke(
            receiverType:TypeToken<T>,
            valueType:TypeToken<V>,
            property: KProperty1<T, List<V>>,
        ): ListProvider<T, V> {
            return ListProvider(receiverType, tokenOf<List<V>>(), PropertyCallable(receiverType, property))
        }

        inline operator fun <T, reified V> invoke(
            receiverType:TypeToken<T>,
            noinline function: Function1<T, List<V>>,
        ): ListProvider<T, V> {
            return ListProvider(receiverType, tokenOf<List<V>>(), FunctionCallable<T, List<V>>(receiverType,  function))
        }

        inline operator fun <reified T, reified V> invoke(
            noinline function: Function1<T, List<V>>,
        ): ListProvider<T, V> {
            return ListProvider(tokenOf<T>(), tokenOf<List<V>>(), FunctionCallable<T, List<V>>(function))
        }
    }
}
