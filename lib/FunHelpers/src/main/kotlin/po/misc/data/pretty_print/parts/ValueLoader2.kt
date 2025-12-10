package po.misc.data.pretty_print.parts

import po.misc.callbacks.signal.Signal
import po.misc.callbacks.signal.signalOf
import po.misc.context.tracable.TraceableContext
import po.misc.data.TextBuilder
import po.misc.functions.NoResult
import po.misc.functions.Throwing
import po.misc.types.getOrThrow
import po.misc.types.k_class.simpleOrAnon
import po.misc.types.safeCast
import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty1


class ValueLoader2<T: Any, V: Any>(
    val hostName: String,
    val typeToken: TypeToken<T>,
    val valueToken: TypeToken<V>,
): TraceableContext, TextBuilder{

    internal var propertyBacking: KProperty1<T, V>? = null
    val property: KProperty1<T, V> get() {
        return propertyBacking.getOrThrow(KProperty1::class)
    }
    val hasProperty: Boolean get() = propertyBacking != null

    internal var providerBacking: (() -> V)? = null
    val provider: () -> V get() {
        return providerBacking.getOrThrow(this)
    }

    val resolved: Signal<ValueLoader2<T, V>, Unit> = signalOf(NoResult)
    var lastValue: V? = null
        private set

    val valueResolved: Signal<V, Unit> = signalOf<V>(valueToken, NoResult)

    val hasProvider: Boolean get() = providerBacking != null

    val canLoadValue: Boolean get() = hasProvider || hasProperty

    internal inline fun <reified TH: Throwable> makeThrow(message: String): Nothing{
        val throwableClass = TH::class
        val msg = "$hostName -> $message"
        when(throwableClass){
            is IllegalStateException -> throw IllegalStateException(msg)
            else -> throw IllegalStateException(msg)
        }
    }

    fun getValue():V? = lastValue
    fun getValue(throwing: Throwing):V{
        val value = lastValue
        if(value != null){
            return value
        }
        val msg = "Unable to get last resolved value"
        makeThrow<IllegalStateException>(msg)
    }

    fun resolveProperty(receiver:T):V?{
        if(hasProperty){
            val value =  property.get(receiver)
            resolved.trigger(this)
            valueResolved(value)
            return value
        }
        return null
    }
    fun resolveProperty(receiver:T, throwing: Throwing):V{
        val value = resolveProperty(receiver)
        if(value != null){
            return value
        }
        val message = "Impossible to resolve value of receiver type ${receiver::class.simpleOrAnon}"
        makeThrow<IllegalStateException>(message)
    }

    fun resolveProvider():V?{
        if(hasProvider){
            val value = provider.invoke()
            resolved.trigger(this)
            valueResolved(value)
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

    fun valueResolved(value: V){
        valueResolved.trigger(value)
    }
    fun setProperty(property: KProperty1<T, V>){
        propertyBacking = property
    }
    fun setProvider(provider: ()-> V){
        providerBacking = provider
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

    fun initFrom(other: ValueLoader2<T, V>){
        if( other.hasProperty){
            propertyBacking = other.propertyBacking
        }
        if(other.hasProvider){
            providerBacking = other.providerBacking
        }
    }
    fun initValueFrom(other: ValueLoader2<*, V>) {
        if(other.typeToken == typeToken && other.hasProperty){
            val castedProperty = other.propertyBacking?.safeCast<KProperty1<T, V>>()
            propertyBacking = castedProperty
        }
        if(other.typeToken == typeToken && other.hasProvider){
            val castedProvider = other.providerBacking?.safeCast<() -> V>()
            providerBacking = castedProvider
        }
    }
}