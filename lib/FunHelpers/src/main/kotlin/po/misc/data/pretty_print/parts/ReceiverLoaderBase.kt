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


class ReceiverLoader<T1: Any>(): ReceiverLoaderBase<Any, Collection<T1>>("ReceiverLoader") {
    fun provideCollectionProperty(property:  KProperty1<Any, Collection<T1>>){
        propertyBacking = property
    }
}



//class ReceiverListLoader<T: Any, V: Any>(
//    val typeToken: TypeToken<V>,
//    override val  property: KProperty1<T,  Collection<V>>,
//): ReceiverLoaderBase<T, Collection<V>>("ReceiverListLoader") {
//
//    private var builderBacking: (PrettyPromiseGrid<T, V>.(List<V>) -> Unit)? = null
//    val builder: PrettyPromiseGrid<T, V>.(List<V>) -> Unit get() {
//        return builderBacking.getOrThrow(this)
//    }
//    init {
//        provideReadOnlyProperty(property)
//    }
//
//    val journal = mutableListOf<Pair<String, Boolean>>()
//
//    fun provideReadOnlyProperty(
//        property: KProperty1<T,  Collection<V>>,
//    ){
//        propertyBacking = property
//    }
//    fun provideBuilder(
//        builder:  PrettyPromiseGrid<T, V>.(List<V>)-> Unit
//    ){
//        builderBacking = builder
//    }
//
//    fun resolveReceiver(receiver:T): List<V> {
//        return property.get(receiver).toList()
//    }
//
//    fun <T1: Any> tryResolveReceiver(receiver: T1): List<V>? {
//
//      return  property.safeCast<KProperty1<T1,  Collection<V>>>()?.let {casted->
//            casted.get(receiver).toList()
//      }?:run {
//          "tryResolveReceiver. Property receiver of wrong type".output(Colour.Yellow)
//          journal.add(Pair("tryResolveReceiver. Property receiver of wrong type", false))
//          null
//      }
//
//    }
//
//    fun applyConfiguration(builderReceiver: PrettyPromiseGrid<T, V>, parameter: List<V>){
//        builderBacking?.let {
//            it.invoke(builderReceiver, parameter)
//            journal.add(Pair("applyConfiguration", true))
//        }?:run {
//            journal.add(Pair("applyConfiguration. Failed builder lambda not provided", false))
//        }
//    }
//
//    fun resolveTemplate(
//        receiver:T,
//        builderReceiver: PrettyPromiseGrid<T, V>
//    ): List<V>{
//       val resultList = resolveReceiver(receiver)
//       applyConfiguration(builderReceiver, resultList)
//        return resultList
//    }
//
//}
