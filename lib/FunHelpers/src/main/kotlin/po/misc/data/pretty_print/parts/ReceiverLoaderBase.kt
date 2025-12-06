package po.misc.data.pretty_print.parts

import po.misc.context.tracable.TraceableContext
import po.misc.functions.Throwing
import po.misc.types.getOrThrow
import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty1


sealed class ReceiverLoaderBase<T: Any,  V>(
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
}


class ValueLoader<T: Any, V: Any>(
    hostName: String,
    val typeToken: TypeToken<T>,
    val valueToken: TypeToken<V>,
):ReceiverLoaderBase<T, V>(hostName){

    fun setReadOnlyProperty(property: KProperty1<T, V>){
        propertyBacking = property
    }
    fun setProvider(provider: ()-> V){
        providerBacking = provider
    }

    fun resolveProperty(receiver:T):V{
        return property.get(receiver)
    }
    fun resolveValue(receiver:T):V?{
        if(hasProvider){
           return provider.invoke()
        }
        if(hasProperty){
            return property.get(receiver)
        }
        return null
    }

    fun resolveValue(receiver:T, throwing: Throwing):V{
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