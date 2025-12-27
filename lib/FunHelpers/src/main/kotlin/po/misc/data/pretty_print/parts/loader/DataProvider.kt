package po.misc.data.pretty_print.parts.loader

import po.misc.collections.lambda_map.CallableDescriptor
import po.misc.collections.lambda_map.CallableRepository
import po.misc.collections.lambda_map.CallableStorage
import po.misc.collections.lambda_map.CallableWrapper
import po.misc.collections.lambda_map.FunctionCallable
import po.misc.collections.lambda_map.PropertyCallable
import po.misc.collections.lambda_map.ReceiverCallable
import po.misc.properties.checkType
import po.misc.reflection.displayName
import po.misc.types.castOrThrow
import po.misc.types.k_function.lambdaName
import po.misc.types.token.TokenFactory
import po.misc.types.token.TokenizedResolver
import po.misc.types.token.TypeToken
import po.misc.types.token.asEffectiveListType
import po.misc.types.token.tokenOf
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1


class DataProvider<T, V>(
    override val receiverType: TypeToken<T>,
    override val valueType: TypeToken<V>,
    val storage: CallableStorage<T, V> = CallableRepository<T, V>(receiverType, valueType)
): TokenizedResolver<T, V>, CallableStorage<T, V> by storage {

    var receiverKey:String = ""

    val propertyCallable: PropertyCallable<T, V>? get() = (storage as CallableRepository<T, V>).getCallable<PropertyCallable<T, V>>()
    val hasProperty: Boolean get() = propertyCallable != null
    val hasListProperty: Boolean get() = valueType.isCollection && propertyCallable != null

    private var listTypProvider: (() -> List<V>)? = null
    private var singleTypeProvider: (() -> V)? = null

    val provider: (() -> V)? get() {
       return if (valueType.isCollection) {
            listTypProvider?.castOrThrow()
        } else {
            singleTypeProvider?.castOrThrow()
        }
    }
    val listProvider: (() -> List<V>)? get() = listTypProvider
    val hasProvider: Boolean get() = singleTypeProvider != null
    val hasListProvider: Boolean get() = listTypProvider != null

    private var singleTypeResolver: ((T) -> V)? = null
    private var listTypResolver: ((T) -> List<V>)? = null

    val resolver: ((T) -> V)? get() {
        return if (valueType.isCollection) {
            listTypResolver?.castOrThrow()
        } else {
            singleTypeResolver?.castOrThrow()
        }
    }
    val listResolver: ((T) -> List<V>)? get() = listTypResolver
    val hasListResolver: Boolean get() = listTypResolver != null
    
    fun addResolver(resolver: Function1<T, V>): DataProvider<T, V>{
        if (valueType.isCollection) {
            val casted = resolver.castOrThrow<(T) -> List<V>>()
            receiverKey = casted::class.lambdaName
            listTypResolver = casted
        } else {
            singleTypeResolver = resolver
            receiverKey = resolver::class.lambdaName
        }
        return this
    }
    fun addProvider(resolver: () -> V): DataProvider<T, V>{
        if (valueType.isCollection) {
            val casted = resolver.castOrThrow<() -> List<V>>()
            receiverKey = casted::class.lambdaName
            listTypProvider = casted
        } else {
            singleTypeProvider = resolver
            receiverKey = resolver::class.lambdaName
        }
        return this
    }

    @PublishedApi
    internal fun resolveProperty(prop: KProperty1<T, V>): DataProvider<T, V> {
        receiverKey = prop.displayName
        storage.add(PropertyCallable(receiverType, valueType,  prop) )
        return this
    }
    @PublishedApi
    internal fun resolveListTypeProperty(prop: KProperty1<T, List<V>>) {
      @Suppress("UNCHECKED_CAST")
      storage.add( PropertyCallable(receiverType, valueType,  prop as  KProperty1<T, V>) )
      receiverKey = prop.displayName
    }
    companion object : TokenFactory{
        inline operator fun <T, reified V> invoke(
            receiverType: TypeToken<T>,
            property: KProperty1<T, V>
        ): DataProvider<T, V> {
            val value =tokenOf<V>()
           return DataProvider(receiverType, value).also {
                it.resolveProperty(property)
                it.add(PropertyCallable(receiverType,  value, property))
            }
        }

        inline operator fun <reified T, reified V> invoke(
            property: KProperty1<T, V>
        ): DataProvider<T, V> = DataProvider(tokenOf<T>(), property)
        /**
         * Creates a receiver-less [DataProvider] backed by a value provider function.
         *
         * In this variant the receiver type and value type are the same ([V]).
         * The provider does not depend on any external receiver and is invoked
         * directly during rendering.
         *
         * This overload is intended for advanced or reflective scenarios where
         * the value type is already represented by a [TypeToken].
         *
         * @param valueType token describing the produced value type
         * @param provider function producing the value
         */
        inline operator fun <reified V: Any> invoke(
            valueClass: KClass<V>,
           noinline  provider: () -> V
        ): DataProvider<V, V> = DataProvider(tokenOf<V>(), tokenOf<V>()).addProvider(provider)

        /**
         * Creates a receiver-less [DataProvider] backed by a value provider function.
         *
         * The value type [V] is inferred from the provided [KClass].
         * This overload is intended for simple DSL usage where only a value
         * needs to be supplied.
         *
         * Example:
         * ```
         * DataProvider(String::class) { "static text" }
         * ```
         *
         * @param valueClass class of the produced value
         * @param provider function producing the value
         */
        operator fun <V: Any> invoke(
            valueType:TypeToken<V>,
            provider:() -> V
        ): DataProvider<V, V> = DataProvider(valueType, valueType).addProvider(provider)

        /**
         * Creates a [DataProvider] backed by a resolver function.
         *
         * The resolver receives the current receiver instance of type [T] and produces
         * a value of type [V].
         *
         * This overload requires an explicit [receiverType] and is intended for
         * generic or reflective contexts where type inference is insufficient.
         *
         * @param receiverType token describing the receiver type [T]
         * @param resolver function mapping a receiver instance to a value
         */
        inline operator fun <T, reified V> invoke(
            receiverType:TypeToken<T>,
            noinline resolver: (T) -> V
        ): DataProvider<T, V> {
            val value = tokenOf<V>()
           return DataProvider(receiverType, value).also {
                it.addResolver(resolver)
                it.add(FunctionCallable(receiverType, value, resolver))
            }
        }
        /**
         * Creates a [DataProvider] backed by a resolver function.
         *
         * The receiver type [T] is inferred directly from the resolver's parameter type.
         * This overload is ideal for DSL usage where the resolver function already
         * expresses the intended receiver.
         *
         * Example:
         * ```
         * DataProvider { receiver: MyType -> receiver.value }
         * ```
         *
         * @param resolver function mapping a receiver instance to a value
         */
        inline operator fun <reified T, reified V> invoke(
            noinline resolver: (T) -> V
        ): DataProvider<T, V> = DataProvider(tokenOf<T>(), resolver)

    }

    override val typeToken: TypeToken<T>
        get() = storage.typeToken
}