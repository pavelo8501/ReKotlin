package po.misc.data.pretty_print.parts

import po.misc.callbacks.signal.Signal
import po.misc.callbacks.signal.signal
import po.misc.callbacks.signal.signalOf
import po.misc.context.tracable.TraceableContext
import po.misc.data.TextBuilder
import po.misc.data.styles.TextStyler
import po.misc.functions.NoResult
import po.misc.functions.Throwing
import po.misc.types.getOrThrow
import po.misc.types.safeCast
import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty1


sealed class ReceiverLoaderBase<T: Any,  V: Any>(
   val hostName: String,
): TraceableContext {

    internal var propertyBacking: KProperty1<T, V>? = null
    open val property: KProperty1<T, V> get() {
        return propertyBacking.getOrThrow(KProperty1::class)
    }
    val hasProperty: Boolean get() = propertyBacking != null

    internal var providerBacking: (() -> V)? = null
    val provider: () -> V get() {
        return providerBacking.getOrThrow(this)
    }

    internal var listProviderBacking: (() -> Collection<V>)? = null
    val listProvider: () -> Collection<V> get() {
        return listProviderBacking.getOrThrow(this)
    }

    val hasProvider: Boolean get() = providerBacking != null || listProviderBacking != null

    val canLoadValue: Boolean get() {
      return  hasProperty || hasProvider
    }

    internal inline fun <reified TH: Throwable> makeThrow(message: String): Nothing{
        val throwableClass = TH::class
        val msg = "$hostName -> $message"
        when(throwableClass){
            is IllegalStateException -> throw IllegalStateException(msg)
            else -> throw IllegalStateException(msg)
        }
    }

    fun initFrom(otherLoader: ReceiverLoaderBase<T, V>){
        propertyBacking = otherLoader.propertyBacking
        providerBacking = otherLoader.providerBacking
        listProviderBacking = otherLoader.listProviderBacking
    }

    fun initValueFrom(otherLoader: ReceiverLoaderBase<*, V>) {
        val castedProperty = otherLoader.propertyBacking?.safeCast<KProperty1<T, V>>()
        propertyBacking = castedProperty
        val castedProvider = otherLoader.providerBacking?.safeCast<() -> V>()
        providerBacking = castedProvider
        val castedListProvider = otherLoader.providerBacking?.safeCast<() -> List<V>>()
        listProviderBacking = castedListProvider
    }

}

class ValueLoader<T: Any, V: Any>(
    hostName: String,
    val typeToken: TypeToken<T>,
    val valueToken: TypeToken<V>,
):ReceiverLoaderBase<T, V>(hostName), TextBuilder{

    val resolved: Signal<ValueLoader<T, V>, Unit> = signalOf(NoResult)
    val valueResolved: Signal<V, Unit> = signalOf<V>(valueToken, NoResult)

    var lastValue: V? = null

    fun getValue():V?{
        return lastValue
    }

    fun getValue(throwing: Throwing):V{
        val value = lastValue
        if(value != null){
            return value
        }
        val msg = "Unable to get last resolved value"
        makeThrow<IllegalStateException>(msg)
    }



    fun setReadOnlyProperty(property: KProperty1<T, V>){
        propertyBacking = property
    }

    fun setProvider(provider: ()-> V){
        providerBacking = provider
    }

    fun resolveProperty(receiver:T):V?{
        if(hasProperty){
            val value =  property.get(receiver)
            resolved.trigger(this)
            valueResolved.trigger(value)
            return value
        }
        return null
    }

    fun resolveProperty(receiver:T, throwing: Throwing):V{
        val value = resolveProperty(receiver)
        if(value != null){
            return value
        }
        val message = "Impossible to resolve value of receiver type ${typeToken.simpleName}"
        makeThrow<IllegalStateException>(message)
    }

    fun resolveProvider():V?{
        if(hasProvider){
            val value = provider.invoke()
            resolved.trigger(this)
            valueResolved.trigger(value)
            return value
        }
        return null
    }

    fun resolveProvider(throwing: Throwing):V{
        val value = resolveProvider()
        if(value != null){
            return value
        }
        val message = "Impossible to resolve by provider. HasProvider: $hasProvider"
        makeThrow<IllegalStateException>(message)
    }

    fun resolveValue(receiver:T):V?{
        val value = resolveProvider()
        if(value != null){
            return value
        }
        return resolveProperty(receiver)
    }

    fun resolveValue(receiver:T, throwing: Throwing):V{
        val value = resolveValue(receiver)
        if(value != null){
            return value
        }
        val message = "Impossible to resolve value of receiver type ${typeToken.simpleName}".concat {
            "HasProperty: $hasProperty HasProvider: $hasProvider"
        }
        makeThrow<IllegalStateException>(message)
    }
}


class ListValueLoader<T: Any, V: Any>(
    hostName: String,
    val typeToken: TypeToken<T>,
    val valueToken: TypeToken<V>,
): ReceiverLoaderBase<T, List<V>>(hostName) {

    fun setReadOnlyProperty(property: KProperty1<T, List<V>>){
        propertyBacking = property
    }
    fun setProvider(provider: ()-> List<V>){
        providerBacking = provider
    }

    fun resolveProperty(receiver:T):List<V>{
        return property.get(receiver)
    }

    fun resolveValue(receiver:T):List<V>{
        if(hasProvider){
            return provider.invoke()
        }
        if(hasProperty){
            return property.get(receiver)
        }
        return emptyList()
    }

    fun resolveValue(receiver:T, throwing: Throwing):List<V>{
        if(hasProvider){
            return provider.invoke()
        }
        if(hasProperty){
            return property.get(receiver)
        }
        val message = "Impossible to resolve value of receiver type ${typeToken.simpleName}"
        makeThrow<IllegalStateException>(message)
    }
}